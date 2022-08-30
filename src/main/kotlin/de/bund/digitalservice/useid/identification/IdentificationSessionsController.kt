package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
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
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val tcTokenService: ITcTokenService,
    private val applicationProperties: ApplicationProperties,
    private val eidService: EidService
) {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(notFoundException: NoSuchElementException): Mono<ResponseEntity<ErrorMessage>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorMessage(HttpStatus.NOT_FOUND.value(), "Could not find session."))
        )
    }

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    fun createSession(
        @RequestBody createIdentitySessionRequest: CreateIdentitySessionRequest,
        serverHttpRequest: ServerHttpRequest,
        authentication: Authentication
    ): Mono<ResponseEntity<CreateIdentitySessionResponse>> {
        return identificationSessionService
            .create(
                refreshAddress = (authentication as ApiKeyAuthenticationToken).details.refreshAddress!!,
                requestAttributes = createIdentitySessionRequest.requestAttributes
            )
            .map {
                val tcTokenUrl = "${applicationProperties.baseUrl}$IDENTIFICATION_SESSIONS_BASE_PATH/${it.useIDSessionId}/$TCTOKEN_PATH_SUFFIX"
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CreateIdentitySessionResponse(tcTokenUrl = tcTokenUrl))
            }
            .doOnError { exception ->
                log.error {
                    "error occurred when creating identification session: ${exception.message}"
                }
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
                tcTokenService.getTcToken(it.refreshAddress)
            }
            .doOnNext {
                val eIDSessionId = UriComponentsBuilder
                    .fromHttpUrl(it.refreshAddress)
                    .encode().build()
                    .queryParams.getFirst("sessionId")
                identificationSessionService.updateEIDSessionId(useIDSessionId, UUID.fromString(eIDSessionId))
            }
            .map {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_XML)
                    .body(it)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .doOnError { exception ->
                log.error { "error occurred while getting tc token for useIDSessionId $useIDSessionId;\n ${exception.message}" }
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping("/{eIDSessionId}")
    fun getIdentity(@PathVariable eIDSessionId: UUID): Mono<ResponseEntity<GetResultResponseType>> {
        return identificationSessionService.findByEIDSessionId(eIDSessionId)
            .map {
                eidService.getEidInformation(eIDSessionId.toString())
            }
            .publishOn(Schedulers.boundedElastic())
            .doOnNext {
                // success case -> remove session from database
                // TODO: REPLACED CONTAINS CONDITION WITH FIXED SUCCESS CASE (e.g. http://www.bsi.bund.de/ecard/api/1.1/resultmajor#success) AS SOON AS WE HAVE E2E SUCCESS FLOW DONE
                if (it.result.resultMajor.contains("api")) {
                    // rework following lines? -> session is fetched before. How can I use the previous value
                    val session = identificationSessionService.findByEIDSessionId(eIDSessionId).block()!!
                    identificationSessionService.delete(session)
                } else {
                    // possibility to catch other cases like "errors" or "data not ready yet"
                }
            }
            .map {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(it)
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).body(null))
            .doOnError { exception ->
                log.error { "error occurred while getting identity data for eIDSessionId $eIDSessionId;\n ${exception.message}" }
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }
}
