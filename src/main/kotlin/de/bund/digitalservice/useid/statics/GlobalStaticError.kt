package de.bund.digitalservice.useid.statics
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.widget.WidgetProperties
import org.springframework.boot.web.error.ErrorAttributeOptions
import org.springframework.boot.web.reactive.error.DefaultErrorAttributes
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.server.ServerRequest
import org.springframework.web.util.UriUtils
import kotlin.text.Charsets.UTF_8

@Component
class GlobalStaticError(
    private val applicationProperties: ApplicationProperties,
    private val globalStaticProperties: GlobalStaticProperties,
    private val widgetProperties: WidgetProperties
) : DefaultErrorAttributes() {

    override fun getErrorAttributes(request: ServerRequest, options: ErrorAttributeOptions): Map<String, Any> {
        val errorAttributes = super.getErrorAttributes(request, options)
        val statusCode = errorAttributes["status"] as Int

        val customGlobalErrorAttributes = mapOf(
            "localizationError" to globalStaticProperties.errorView.localization,
            "errorReportEmailLink" to createEmailReportLink(statusCode),
            "baseUrl" to applicationProperties.baseUrl,
            "metaTag" to widgetProperties.metaTag
        )

        return errorAttributes + customGlobalErrorAttributes
    }

    private fun createEmailReportLink(statusCode: Int): String {
        val emailAddress = globalStaticProperties.errorView.localization.errorReportEmail
        val errorReportSubject = globalStaticProperties.errorView.localization.errorReportSubject
        val errorReportBody = globalStaticProperties.errorView.localization.errorReportBody

        /*
            URLEncoder.encode() will encode whitespace to "+" instead of %20 which will not work for email link,
            it would replace whitespace with "+" sign in the e-mail body. UriUtils.encode() from Spring encodes
            whitespace to "%20"
            RFC Document: https://www.rfc-editor.org/rfc/rfc6068#section-5
         */
        val encodedEmailSubject = UriUtils.encode("$errorReportSubject $statusCode", UTF_8)
        val encodedBody = UriUtils.encode(errorReportBody, UTF_8)

        return "mailto:$emailAddress?subject=$encodedEmailSubject&body=$encodedBody"
    }
}
