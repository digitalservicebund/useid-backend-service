package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.apikeys.ApiKeyDetails
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.config.METRIC_NAME_EID_SERVICE_REQUESTS
import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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
@RequestMapping(IDENTIFICATION_SESSIONS_BASE_PATH)
@Tag(name = "Identification Sessions", description = "An identification session represent an ongoing identification flow of a user and stores the required information.")
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val applicationProperties: ApplicationProperties,
    private val eidServiceConfig: EidServiceConfiguration
) {
    private val log = KotlinLogging.logger {}
    private val tcTokenCallsSuccessfulCounter: Counter = Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_tc_token", "status", "200")
    private val tcTokenCallsWithErrorsCounter: Counter = Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_tc_token", "status", "500")
    private val getEidInformationCallsSuccessfulCounter: Counter = Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "200")
    private val getEidInformationCallsWithErrorsCounter: Counter = Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "500")

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create session as eService")
    fun createSession(
        authentication: Authentication
    ): Mono<ResponseEntity<CreateIdentificationSessionResponse>> {
        val apiKeyDetails = authentication.details as ApiKeyDetails
        return identificationSessionService.create(apiKeyDetails.refreshAddress!!, apiKeyDetails.requestDataGroups)
            .map {
                val tcTokenUrl =
                    "${applicationProperties.baseUrl}$IDENTIFICATION_SESSIONS_BASE_PATH/${it.useIdSessionId}/$TCTOKEN_PATH_SUFFIX"
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
        path = ["/{useIdSessionId}/$TCTOKEN_PATH_SUFFIX"],
        produces = [MediaType.APPLICATION_XML_VALUE]
    )
    @Operation(summary = "Get TC token for this session")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", description = "No corresponding session found for that useIdSessionId", content = [Content()])
    fun getTCToken(@PathVariable useIdSessionId: UUID): Mono<ResponseEntity<TCTokenType>> {
        return identificationSessionService.findByUseIdSessionId(useIdSessionId)
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
                val eIdSessionId = UriComponentsBuilder
                    .fromHttpUrl(it.refreshAddress)
                    .encode().build()
                    .queryParams.getFirst("sessionId")
                identificationSessionService.updateEIDSessionId(useIdSessionId, UUID.fromString(eIdSessionId))
            }
            .map {
                tcTokenCallsSuccessfulCounter.increment()
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(it.t1)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .doOnError { exception ->
                tcTokenCallsWithErrorsCounter.increment()
                log.error("Failed to get tc token for identification session. useIdSessionId=$useIdSessionId", exception)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping("/{eIdSessionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Fetch data as eService after identification was successful")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "404", description = "No corresponding session found for that eIdSessionId", content = [Content()])
    @ApiResponse(responseCode = "401", description = "Authentication failed (missing or wrong api key)", content = [Content()])
    fun getIdentity(@PathVariable eIdSessionId: UUID, authentication: Authentication): Mono<ResponseEntity<GetResultResponseType>> {
        /*
            Wrapping blocking call to the SDK into Mono.fromCallable
            https://projectreactor.io/docs/core/release/reference/index.html#faq.wrap-blocking
        */
        val apiKeyDetails = authentication.details as ApiKeyDetails
        val getIdentityResult = Mono.fromCallable {
            val eidService = EidService(eidServiceConfig)
            eidService.getEidInformation(eIdSessionId.toString())
        }
        return identificationSessionService.findByEIDSessionId(eIdSessionId)
            .doOnNext {
                if (apiKeyDetails.refreshAddress != it.refreshAddress) {
                    throw SessionAuthenticationException("API key differs from the API key used to start the identification session.")
                }
            }
            .zipWith(getIdentityResult).subscribeOn(Schedulers.boundedElastic())
            .doOnError { exception ->
                getEidInformationCallsWithErrorsCounter.increment()
                log.error("Failed to fetch identity data: ${exception.message}.")
            }
            .doOnNext {
                val identificationSession: IdentificationSession = it.t1
                val result = it.t2.result
                // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
                if (result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
                    getEidInformationCallsSuccessfulCounter.increment()
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
