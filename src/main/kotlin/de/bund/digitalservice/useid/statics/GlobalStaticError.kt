package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.config.ApplicationProperties
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.util.UriUtils
import kotlin.text.Charsets.UTF_8

@Component
class GlobalStaticError(
    private val applicationProperties: ApplicationProperties,
    private val messageSource: MessageSource
) : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val errorAttributes = super.getErrorAttributes(request, options)
        val statusCode = errorAttributes["status"] as Int

        val customGlobalErrorAttributes = mapOf(
            "showReportEmail" to true,
            "errorReportEmailLink" to createEmailReportLink(statusCode),
            "baseUrl" to applicationProperties.baseUrl
        )

        return errorAttributes + customGlobalErrorAttributes
    }

    private fun createEmailReportLink(statusCode: Int): String {
        val emailAddress = messageSource.getMessage("error.default.report-email", null, LocaleContextHolder.getLocale())
        val errorReportSubject = messageSource.getMessage("error.default.report-subject", arrayOf(statusCode), LocaleContextHolder.getLocale())
        val errorReportBody = messageSource.getMessage("error.default.report-body", arrayOf(statusCode), LocaleContextHolder.getLocale())

        /*
            URLEncoder.encode() will encode whitespace to "+" instead of %20 which will not work for email link,
            it would replace whitespace with "+" sign in the e-mail body. UriUtils.encode() from Spring encodes
            whitespace to "%20"
            RFC Document: https://www.rfc-editor.org/rfc/rfc6068#section-5
         */
        val encodedEmailSubject = UriUtils.encode(errorReportSubject, UTF_8)
        val encodedBody = UriUtils.encode(errorReportBody, UTF_8)

        return "mailto:$emailAddress?subject=$encodedEmailSubject&body=$encodedBody"
    }
}
