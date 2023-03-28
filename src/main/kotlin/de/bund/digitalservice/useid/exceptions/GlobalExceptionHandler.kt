package de.bund.digitalservice.useid.exceptions

import com.yubico.webauthn.exception.AssertionFailedException
import de.bund.digitalservice.useid.apikeys.InvalidApiKeyException
import de.bund.digitalservice.useid.credentials.CredentialNotFoundException
import de.bund.digitalservice.useid.events.WidgetNotFoundException
import de.bund.digitalservice.useid.identification.IdentificationSessionNotFoundException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(value = [WidgetNotFoundException::class, IdentificationSessionNotFoundException::class, CredentialNotFoundException::class])
    fun handleNotFound(e: Exception): ResponseEntity<Any> {
        log.info("${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @ExceptionHandler(value = [AssertionFailedException::class, InvalidApiKeyException::class])
    fun handleUnauthorized(e: Exception): ResponseEntity<Any> {
        log.info("${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
