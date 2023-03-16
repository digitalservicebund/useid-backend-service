package de.bund.digitalservice.useid.exceptions

import com.yubico.webauthn.exception.AssertionFailedException
import de.bund.digitalservice.useid.credentials.CredentialNotFoundException
import de.bund.digitalservice.useid.events.WidgetNotFoundException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler

@ControllerAdvice
class GlobalExceptionHandler : ResponseEntityExceptionHandler() {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(WidgetNotFoundException::class)
    fun handleWidgetNotFoundException(e: Exception, request: WebRequest): ResponseEntity<Any> {
        log.info("Could not find widget: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @ExceptionHandler(CredentialNotFoundException::class)
    fun handleCredentialNotFoundException(e: Exception, request: WebRequest): ResponseEntity<Any> {
        log.info("Could not find credential: ${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @ExceptionHandler(AssertionFailedException::class)
    fun handleAssertionFailedException(e: Exception, request: WebRequest): ResponseEntity<Any> {
        log.info("Assertion failed: ${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }
}
