package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.UUID

@RestController
@RequestMapping("/api/v1/identification/sessions")
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val tcTokenUrlService: TcTokenUrlService
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
    fun createSession(@RequestBody createIdentitySessionRequest: CreateIdentitySessionRequest): Mono<ResponseEntity<CreateIdentitySessionResponse>> {
        // Currently returns a mock session response
        return tcTokenUrlService.getTcTokenUrl()
            .flatMap { tcTokenUrl ->
                identificationSessionService.save(
                    tcTokenUrl,
                    createIdentitySessionRequest.refreshAddress,
                    createIdentitySessionRequest.requestAttributes
                )
            }.map {
                ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(
                        CreateIdentitySessionResponse(
                            tcTokenUrl = it.tcTokenUrl,
                            sessionId = it.sessionId
                        )
                    )
            }.doOnError { exception ->
                log.error { "error occurred when creating identification session: ${exception.message}" }
            }.onErrorReturn(
                ResponseEntity
                    .internalServerError()
                    .body(null)
            )
    }

    @GetMapping("/{sessionId}")
    fun getIdentity(@PathVariable sessionId: UUID): Mono<IdentityAttributes> {
        // Currently mock identity
        return Mono.just(IdentityAttributes("firstname", "lastname"))
            .filter {
                identificationSessionService.sessionExists(sessionId)
            }.switchIfEmpty(Mono.error { throw NoSuchElementException() })
    }
}
