package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.tracking.matomo.MatomoEvent
import io.micrometer.core.annotation.Timed
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import ua_parser.Client
import ua_parser.Parser
import java.net.URLEncoder
import kotlin.text.Charsets.UTF_8

internal const val WIDGET_PAGE = "widget"
internal const val INCOMPATIBLE_PAGE = "incompatible"
internal const val FALLBACK_PAGE = "eID-Client"
internal const val WIDGET_START_IDENT_BTN_CLICKED = "start-ident-button-clicked"

@Controller
@Timed
class WidgetController(
    private val applicationProperties: ApplicationProperties,
    private val widgetTracking: WidgetTracking,
    private val applicationEventPublisher: ApplicationEventPublisher,
) {
    private val defaultViewHeaderConfig = mapOf(
        "baseUrl" to applicationProperties.baseUrl,
    )

    @PostMapping("/$WIDGET_START_IDENT_BTN_CLICKED")
    fun handleStartIdentButtonClicked(@RequestParam(required = false, name = "hash") sessionHash: String?, @RequestAttribute tenantId: String?, @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) userAgent: String?): ResponseEntity<String> {
        publishMatomoEvent(
            widgetTracking.actions.buttonPressed,
            widgetTracking.names.startIdent,
            sessionHash,
            userAgent,
            tenantId,
        )
        return ResponseEntity.status(HttpStatus.OK).body("")
    }

    @GetMapping("/$WIDGET_PAGE")
    fun getWidgetPage(
        model: Model,
        @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) userAgent: String?,
        @RequestParam hostname: String,
        @RequestParam(required = false, name = "hash") sessionHash: String?,
        @RequestAttribute tenantId: String?,
    ): ModelAndView {
        publishMatomoEvent(
            widgetTracking.actions.loaded,
            widgetTracking.names.widget,
            sessionHash,
            userAgent,
            tenantId,
        )

        if (isIncompatibleOSVersion(userAgent)) {
            return handleRequestWithIncompatibleOSVersion(sessionHash, userAgent, tenantId)
        }

        val widgetViewConfig = mapOf(
            setEiDClientURL("#"),
            "isWidget" to true,
            "additionalClass" to "",
        )

        val modelAndView = ModelAndView(WIDGET_PAGE)
        modelAndView.addAllObjects(defaultViewHeaderConfig + widgetViewConfig)
        return modelAndView
    }

    @GetMapping("/$FALLBACK_PAGE")
    fun getUniversalLinkFallbackPage(model: Model, @RequestParam tcTokenURL: String, @RequestParam(required = false, name = "hash") sessionHash: String?, @RequestAttribute tenantId: String?, @RequestHeader(name = HttpHeaders.USER_AGENT, required = false) userAgent: String?): ModelAndView {
        publishMatomoEvent(
            widgetTracking.actions.loaded,
            widgetTracking.names.fallback,
            sessionHash,
            userAgent,
            tenantId,
        )
        /*
            Documentation about the link syntax can be found in Technical Guideline TR-03124-1 – eID-Client, Part 1:
            Specifications Version 1.4 8. October 2021, Chapter 2.2 Full eID-Client
            Note: Replaced the prefix eid:// with bundesident:// to make sure only the BundesIdent app is opened
         */
        val url = "bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=${URLEncoder.encode(tcTokenURL, UTF_8)}"

        val widgetViewFallbackConfig = mapOf(
            setEiDClientURL(url),
            "isFallback" to true,
            "additionalClass" to "fallback",
        )

        val modelAndView = ModelAndView(WIDGET_PAGE)
        modelAndView.addAllObjects(defaultViewHeaderConfig + widgetViewFallbackConfig)
        return modelAndView
    }

    private fun publishMatomoEvent(action: String, name: String, sessionId: String?, userAgent: String?, tenantId: String?) {
        val matomoEvent = MatomoEvent(this, widgetTracking.categories.widget, action, name, sessionId, userAgent, tenantId)
        applicationEventPublisher.publishEvent(matomoEvent)
    }

    private fun setEiDClientURL(url: String): Pair<String, String> {
        return "eidClientURL" to url
    }

    private fun isIncompatibleOSVersion(userAgent: String?): Boolean {
        return try {
            val parsedUserAgent = Parser().parse(userAgent)
            val incompatibleIOSVersion = hasIncompatibleMajorVersion(parsedUserAgent, "iOS", 15)
            val incompatibleAndroidVersion = hasIncompatibleMajorVersion(parsedUserAgent, "Android", 9)

            incompatibleIOSVersion || incompatibleAndroidVersion
        } catch (exception: Exception) {
            false
        }
    }

    private fun hasIncompatibleMajorVersion(parsedUserAgent: Client, osFamily: String, supportedMajorVersion: Int): Boolean {
        return parsedUserAgent.os.family == osFamily &&
            !parsedUserAgent.os.major.isNullOrEmpty() &&
            Integer.parseInt(parsedUserAgent.os.major) < supportedMajorVersion
    }

    private fun handleRequestWithIncompatibleOSVersion(sessionHash: String?, userAgent: String?, tenantId: String?): ModelAndView {
        publishMatomoEvent(
            widgetTracking.actions.loaded,
            widgetTracking.names.incompatible,
            sessionHash,
            userAgent,
            tenantId,
        )
        val modelAndView = ModelAndView(INCOMPATIBLE_PAGE)
        modelAndView.addAllObjects(defaultViewHeaderConfig)
        return modelAndView
    }
}
