package de.bund.digitalservice.useid.identification

import de.bos_bremen.gov.autent.common.Utils
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
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
import java.net.URLEncoder
import java.util.UUID

internal const val IDENTIFICATION_SESSIONS_ENDPOINT = "/api/v1/identification/sessions"
internal const val TCTOKEN_ENDPOINT = "tc-token"

@RestController
@RequestMapping(IDENTIFICATION_SESSIONS_ENDPOINT)
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val tcTokenService: TcTokenService
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

    @PostMapping
    fun createSession(
        @RequestBody createIdentitySessionRequest: CreateIdentitySessionRequest,
        serverHttpRequest: ServerHttpRequest
    ): Mono<ResponseEntity<CreateIdentitySessionResponse>> {
        return identificationSessionService
            .save(
                refreshAddress = "https://localhost:8443/", // Currently a mock value, later will inject from env var mapped by API Key
                requestAttributes = createIdentitySessionRequest.requestAttributes
            ) // PublishOn is suggested by IntelliJ, maybe there is another way to resolve blocking URLEncoder.encode()
            .publishOn(Schedulers.boundedElastic())
            .map {
                val encodedTcTokenUrl = URLEncoder.encode("${serverHttpRequest.uri}/$TCTOKEN_ENDPOINT/${it.useIDSessionId}", Utils.ENCODING)
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CreateIdentitySessionResponse(tcTokenUrl = encodedTcTokenUrl))
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

    @GetMapping("/$TCTOKEN_ENDPOINT/{useIDSessionId}")
    fun getTCToken(@PathVariable useIDSessionId: UUID): Mono<ResponseEntity<TCTokenType>> {
        return identificationSessionService.findByIdOrFail(useIDSessionId)
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
            .doOnError { exception ->
                log.error { "error occurred while getting the tc token for session with id $useIDSessionId; ${exception.message}" }
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().body(null)
            )
    }

    @GetMapping("/{eIDSessionId}")
    fun getIdentity(@PathVariable eIDSessionId: UUID): Mono<IdentityAttributes> {
        // Currently, mock identity
        return Mono.just(IdentityAttributes("firstname", "lastname"))
            .filter {
                identificationSessionService.sessionExists(eIDSessionId)
            }
            .switchIfEmpty(Mono.error { throw NoSuchElementException() })
    }
}
