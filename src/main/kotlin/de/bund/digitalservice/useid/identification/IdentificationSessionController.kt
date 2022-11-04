package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.apikeys.ApiKeyDetails
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.micrometer.core.annotation.Timed
import io.micrometer.prometheus.PrometheusMeterRegistry
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.security.web.authentication.session.SessionAuthenticationException
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.util.UriComponentsBuilder
import reactor.core.publisher.Mono
import reactor.core.scheduler.Schedulers
import java.util.UUID

internal const val IDENTIFICATION_SESSIONS_BASE_PATH = "/api/v1/identification/sessions"
internal const val TCTOKEN_PATH_SUFFIX = "tc-token"

@RestController
@Timed
@RequestMapping(IDENTIFICATION_SESSIONS_BASE_PATH)
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val applicationProperties: ApplicationProperties,
    private val eidServiceConfig: EidServiceConfiguration,
    private val metricRegistry: PrometheusMeterRegistry
) {
    private val log = KotlinLogging.logger {}

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createSession(
        authentication: Authentication
    ): Mono<ResponseEntity<CreateIdentificationSessionResponse>> {
        val apiKeyDetails = authentication.details as ApiKeyDetails
        return identificationSessionService.create(apiKeyDetails.refreshAddress!!, apiKeyDetails.requestDataGroups)
            .map {
                val tcTokenUrl =
                    "${applicationProperties.baseUrl}$IDENTIFICATION_SESSIONS_BASE_PATH/${it.useidSessionId}/$TCTOKEN_PATH_SUFFIX"
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CreateIdentificationSessionResponse(tcTokenUrl))
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping(
        path = ["/{useIDSessionId}/$TCTOKEN_PATH_SUFFIX"],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    fun getTCToken(@PathVariable useIDSessionId: UUID): Mono<ResponseEntity<TCTokenType>> {
        return identificationSessionService.findByUseIDSessionId(useIDSessionId)
            .flatMap {
                /*
                    Wrapping blocking call to the SDK into Mono.fromCallable
                    https://projectreactor.io/docs/core/release/reference/index.html#faq.wrap-blocking
                */
                Mono.fromCallable {
                    val eidService = EidService(eidServiceConfig, it.requestDataGroups)
                    eidService.getTcToken("${applicationProperties.baseUrl}$REFRESH_PATH")
                }.subscribeOn(Schedulers.boundedElastic())
            }
            .zipWhen {
                val eIDSessionId = UriComponentsBuilder
                    .fromHttpUrl(it.refreshAddress)
                    .encode().build()
                    .queryParams.getFirst("sessionId")
                identificationSessionService.updateEIDSessionId(useIDSessionId, UUID.fromString(eIDSessionId))
            }
            .map {
                metricRegistry.counter("eid_service.requests.get_tc_token","method", "get_tc_token", "status", "200").increment()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(it.t1)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .doOnError { exception ->
                metricRegistry.counter("eid_service.requests.get_tc_token", "method", "get_tc_token", "status", "500").increment()
                log.error("Failed to get tc token for identification session. useidSessionId=$useIDSessionId", exception)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping("/{eIDSessionId}")
    fun getIdentity(@PathVariable eIDSessionId: UUID, authentication: Authentication): Mono<ResponseEntity<GetResultResponseType>> {
        /*
            Wrapping blocking call to the SDK into Mono.fromCallable
            https://projectreactor.io/docs/core/release/reference/index.html#faq.wrap-blocking
        */
        val apiKeyDetails = authentication.details as ApiKeyDetails
        val getIdentityResult = Mono.fromCallable {
            val eidService = EidService(eidServiceConfig)
            eidService.getEidInformation(eIDSessionId.toString())
        }
        return identificationSessionService.findByEIDSessionId(eIDSessionId)
            .doOnNext {
                if (apiKeyDetails.refreshAddress != it.refreshAddress) {
                    throw SessionAuthenticationException("API key differs from the API key used to start the identification session.")
                }
            }
            .zipWith(getIdentityResult).subscribeOn(Schedulers.boundedElastic())
            .doOnError { exception ->
                metricRegistry.counter("eid_service.requests.get_tc_token","method", "get_eid_information", "status", "500").increment()
                log.error("Failed to fetch identity data: ${exception.message}.")
            }
            .doOnNext {
                val identificationSession: IdentificationSession = it.t1
                val result = it.t2.result
                // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
                if (result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
                    metricRegistry.counter("eid_service.requests","method", "get_eid_information", "status", "200").increment()
                    identificationSessionService.delete(identificationSession)
                        .doOnError { log.error("Failed to delete identification session. id=${identificationSession.id}") }
                        .subscribe()
                } else {
                    // resultMinor error codes can be found in TR 03130 Part 1 -> 3.4.1 Error Codes
                    log.info("The resultMinor for identification session is ${result.resultMinor}. id=${identificationSession.id}")
                }
            }
            .map {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(it.t2)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .onErrorReturn({ e -> e is SessionAuthenticationException }, ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null))
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }
}
