package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.model.ErrorMessage
import de.bund.digitalservice.useid.model.IdentityAttributes
import de.bund.digitalservice.useid.model.SessionResponse
import de.bund.digitalservice.useid.service.IdentityService
import de.bund.digitalservice.useid.service.SessionService
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

@RestController
@RequestMapping("/api/v1/identification/sessions")
class IdentificationSessionsController(
    private val identityService: IdentityService,
    private val sessionService: SessionService
) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(notFoundException: NoSuchElementException): Mono<ResponseEntity<ErrorMessage>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorMessage(HttpStatus.NOT_FOUND.value(), notFoundException.message!!))
        )
    }

    @PostMapping
    fun createSession(@RequestBody clientRequestSession: ClientRequestSession): Mono<SessionResponse> {
        return sessionService.createSession(clientRequestSession)
    }

    @GetMapping("/{sessionId}")
    fun getIdentity(@PathVariable sessionId: String): Mono<IdentityAttributes> {
        return identityService.getIdentity(sessionId)
    }
}
