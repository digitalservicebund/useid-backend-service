package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.config.ApplicationProperties
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.context.i18n.LocaleContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.context.request.WebRequest
import org.springframework.web.util.UriUtils
import kotlin.text.Charsets.UTF_8

@Component
class GlobalStaticError(
    private val applicationProperties: ApplicationProperties,
    private val messageSource: MessageSource,
) : DefaultErrorAttributes() {

    override fun getErrorAttributes(
        webRequest: WebRequest?,
        options: ErrorAttributeOptions?,
    ): MutableMap<String, Any>? {
        val errorAttributes = super.getErrorAttributes(webRequest, options)
        val statusCode = errorAttributes["status"] as Int

        errorAttributes["showReportEmail"] = true
        errorAttributes["errorReportEmailLink"] = createEmailReportLink(statusCode)
        errorAttributes["baseUrl"] = applicationProperties.baseUrl

        return errorAttributes
    }

    private fun createEmailReportLink(statusCode: Int): String {
        val locale = LocaleContextHolder.getLocale()
        val emailAddress = messageSource.getMessage("error.default.report-email", null, locale)
        val errorReportSubject = messageSource.getMessage("error.default.report-subject", arrayOf(statusCode), locale)
        val errorReportBody = messageSource.getMessage("error.default.report-body", arrayOf(statusCode), locale)

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
