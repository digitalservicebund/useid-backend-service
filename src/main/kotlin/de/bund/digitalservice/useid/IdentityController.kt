package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestIdentity
import de.bund.digitalservice.useid.model.ErrorMessage
import de.bund.digitalservice.useid.model.IdentityAttributes
import de.bund.digitalservice.useid.service.IdentityService
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1")
class IdentityController(private val identityService: IdentityService) {

    @ExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(notFoundException: NoSuchElementException): Mono<ResponseEntity<ErrorMessage>> {
        return Mono.just(
            ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ErrorMessage(HttpStatus.NOT_FOUND.value(), notFoundException.message!!))
        )
    }

    @PostMapping(
        path = ["/identity"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun requestIdentity(@Valid @RequestBody clientRequestIdentity: ClientRequestIdentity): Mono<IdentityAttributes> {
        return identityService.getIdentity(clientRequestIdentity.sessionId)
    }
}
