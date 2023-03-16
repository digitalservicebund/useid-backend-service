package de.bund.digitalservice.useid.errors

import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import java.lang.Exception

@ControllerAdvice
class RestExceptionHandler(private val errorAttributes: ErrorAttributes) : ResponseEntityExceptionHandler() {
    @ExceptionHandler(NotFoundException::class)
    protected fun handleNotFoundException(ex: NotFoundException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionInternal(ex, null, HttpHeaders(), HttpStatus.NOT_FOUND, request)

    @ExceptionHandler(UnauthorizedException::class)
    protected fun handleUnauthorizedException(ex: UnauthorizedException, request: WebRequest): ResponseEntity<Any> =
        handleExceptionInternal(ex, null, HttpHeaders(), HttpStatus.UNAUTHORIZED, request)

    override fun handleExceptionInternal(
        ex: Exception,
        body: Any?,
        headers: HttpHeaders,
        status: HttpStatus,
        request: WebRequest,
    ): ResponseEntity<Any> = super.handleExceptionInternal(
        ex,
        body ?: errorAttributes.getErrorAttributes(request, ErrorAttributeOptions.defaults()),
        headers,
        status,
        request,
    )
}
