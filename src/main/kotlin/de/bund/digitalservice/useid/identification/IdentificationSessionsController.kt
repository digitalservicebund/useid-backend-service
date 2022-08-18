package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationToken
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
import java.util.UUID

internal const val IDENTIFICATION_SESSIONS_BASE_PATH = "/api/v1/identification/sessions"
internal const val TCTOKEN_PATH_SUFFIX = "tc-token"

@RestController
@RequestMapping(IDENTIFICATION_SESSIONS_BASE_PATH)
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val tcTokenService: ITcTokenService
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
                /* TODO: Find a way to build the url, by using serverHttpRequest.uri is not preferable
                    since the path parameter can be different (in case of in proxy server)
                 */
                val tcTokenUrl = "${serverHttpRequest.uri}/${it.useIDSessionId}/$TCTOKEN_PATH_SUFFIX"
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
        return identificationSessionService.findById(useIDSessionId)
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
