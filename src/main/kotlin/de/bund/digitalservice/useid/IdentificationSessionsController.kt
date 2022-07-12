package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.datasource.IdentificationSessionsDataSource
import de.bund.digitalservice.useid.model.CreateIdentitySessionRequest
import de.bund.digitalservice.useid.model.CreateIdentitySessionResponse
import de.bund.digitalservice.useid.model.ErrorMessage
import de.bund.digitalservice.useid.model.IdentityAttributes
import de.bund.digitalservice.useid.service.IdentificationSessionService
import de.bund.digitalservice.useid.service.IdentityService
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
import reactor.kotlin.core.publisher.switchIfEmpty

@RestController
@RequestMapping("/api/v1/identification/sessions")
class IdentificationSessionsController(
    private val identityService: IdentityService,
    private val identificationSessionService: IdentificationSessionService,
    private val identificationSessionsDataSource: IdentificationSessionsDataSource
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
    fun createSession(@RequestBody createIdentitySessionRequest: CreateIdentitySessionRequest): Mono<CreateIdentitySessionResponse> {
        return identificationSessionService
            .createSession(createIdentitySessionRequest)
            .doOnNext { sessionResponse ->
                identificationSessionsDataSource.addSession(sessionResponse)
            }
    }

    @GetMapping("/{sessionId}")
    fun getIdentity(@PathVariable sessionId: String): Mono<IdentityAttributes> {
        return identityService
            .getIdentity(sessionId)
            .filter {
                identificationSessionsDataSource.getSession().any { it.sessionId == sessionId }
            }.doOnNext {
                identificationSessionsDataSource.removeSession(sessionId)
            }.switchIfEmpty {
                Mono.error { throw NoSuchElementException("Error: sessionId is not found") }
            }
    }
}
