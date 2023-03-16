package de.bund.digitalservice.useid.exceptions

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
}
