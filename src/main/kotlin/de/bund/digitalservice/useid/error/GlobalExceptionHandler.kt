package de.bund.digitalservice.useid.error

import de.bund.digitalservice.useid.credentials.CredentialNotFoundException
import de.bund.digitalservice.useid.credentials.InvalidCredentialException
import de.bund.digitalservice.useid.eventstreams.EventStreamNotFoundException
import de.bund.digitalservice.useid.identification.IdentificationSessionNotFoundException
import de.bund.digitalservice.useid.tenant.InvalidTenantException
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import java.net.SocketTimeoutException

@ControllerAdvice
class GlobalExceptionHandler {
    private val log = KotlinLogging.logger {}

    @ExceptionHandler(
        value = [
            EventStreamNotFoundException::class,
            IdentificationSessionNotFoundException::class,
            CredentialNotFoundException::class,
        ],
    )
    fun handleNotFound(e: Exception): ResponseEntity<Any> {
        log.info("${e.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
    }

    @ExceptionHandler(
        value = [
            InvalidCredentialException::class,
            InvalidTenantException::class,
        ],
    )
    fun handleUnauthorized(e: Exception): ResponseEntity<Any> {
        log.info("${e.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    @ExceptionHandler(
        value = [
            SocketTimeoutException::class,
        ],
    )
    fun handleSocketTimeout(e: Exception): ResponseEntity<Any> {
        log.error("Unexpected timeout during web service request.", e)
        return ResponseEntity.status(HttpStatus.GATEWAY_TIMEOUT).build()
    }
}
