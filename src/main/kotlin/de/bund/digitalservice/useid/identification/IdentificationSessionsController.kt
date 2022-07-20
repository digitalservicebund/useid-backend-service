package de.bund.digitalservice.useid.identification

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
    private val identificationSessionHandler: IdentificationSessionHandler
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
        // Currently returns a mock session response
        return Mono.just(
            CreateIdentitySessionResponse("http://127.0.0.1:24727/eID-Client?tcTokenURL=mock", UUID.randomUUID().toString())
        ).doOnNext {
            identificationSessionHandler.save(
                it.sessionId,
                it.tcTokenUrl,
                createIdentitySessionRequest.refreshAddress,
                createIdentitySessionRequest.requestAttributes
        return tcTokenUrlService.getTcTokenUrl()
            )
        }
    }

    @GetMapping("/{sessionId}")
    fun getIdentity(@PathVariable sessionId: String): Mono<IdentityAttributes> {
        // Currently mock identity
        return Mono.just(IdentityAttributes("firstname", "lastname"))
            .filter {
                identificationSessionHandler.hasValidSessionId(sessionId)
            }
    }
}
