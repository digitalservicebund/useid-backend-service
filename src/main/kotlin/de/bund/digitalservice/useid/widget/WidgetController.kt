package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.tracking.matomo.MatomoEvent
import io.micrometer.core.annotation.Timed
import org.springframework.context.ApplicationEventPublisher
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.result.view.Rendering
import ua_parser.Client
import ua_parser.Parser

internal const val WIDGET_PAGE = "widget"
internal const val INCOMPATIBLE_PAGE = "incompatible"
internal const val FALLBACK_PAGE = "eID-Client"
internal const val WIDGET_START_IDENT_BTN_CLICKED = "start-ident-button-clicked"

@Controller
@Timed
class WidgetController(
    private val applicationProperties: ApplicationProperties,
    private val widgetProperties: WidgetProperties,
    private val widgetTracking: WidgetTracking,
    private val applicationEventPublisher: ApplicationEventPublisher
) {
    private val defaultViewHeaderConfig = mapOf(
        "baseUrl" to applicationProperties.baseUrl,
        "metaTag" to widgetProperties.metaTag
    )

    @PostMapping("/$WIDGET_START_IDENT_BTN_CLICKED")
    fun handleStartIdentButtonClicked(@RequestParam(required = false, name = "hash") sessionHash: String?): ResponseEntity<String> {
        publishMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.buttonPressed,
            widgetTracking.names.startIdent,
            sessionHash
        )
        return ResponseEntity.status(HttpStatus.OK).body("")
    }

    @GetMapping("/$WIDGET_PAGE")
    fun getWidgetPage(
        model: Model,
        @RequestHeader("User-Agent") userAgent: String,
        @RequestParam() hostname: String,
        @RequestParam(required = false, name = "hash") sessionHash: String?
    ): Rendering {
        publishMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.loaded,
            widgetTracking.names.widget,
            sessionHash
        )

        val widgetViewConfig = mapOf(
            setMainViewLocalization(),
            setMainViewMobileURL(),
            setEiDClientURL("#"),
            "isWidget" to true,
            "additionalClass" to ""
        )

        if (isIncompatibleOSVersion(userAgent)) {
            return handleRequestWithIncompatibleOSVersion()
        }

        return Rendering
            .view(WIDGET_PAGE)
            .model(defaultViewHeaderConfig + widgetViewConfig)
            .status(HttpStatus.OK)
            .build()
    }

    @GetMapping("/$FALLBACK_PAGE")
    fun getUniversalLinkFallbackPage(model: Model, @RequestParam() tcTokenURL: String, @RequestParam(required = false, name = "hash") sessionHash: String?): Rendering {
        publishMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.loaded,
            widgetTracking.names.fallback,
            sessionHash
        )
        /*
            Documentation about the link syntax can be found in Technical Guideline TR-03124-1 – eID-Client, Part 1:
            Specifications Version 1.4 8. October 2021, Chapter 2.2 Full eID-Client
            Note: Replaced the prefix eid:// with bundesident:// to make sure only the BundesIdent app is opened
         */
        val url = "bundesident://127.0.0.1:24727/eID-Client?tcTokenURL=$tcTokenURL"

        val widgetViewFallbackConfig = mapOf(
            setMainViewLocalization(),
            setMainViewMobileURL(),
            setEiDClientURL(url),
            "localizationError" to widgetProperties.errorView.fallback.localization,
            "additionalClass" to "fallback"
        )

        return Rendering
            .view(WIDGET_PAGE)
            .model(defaultViewHeaderConfig + widgetViewFallbackConfig)
            .status(HttpStatus.OK)
            .build()
    }

    private fun publishMatomoEvent(category: String, action: String, name: String, sessionId: String?) {
        val matomoEvent = MatomoEvent(this, category, action, name, sessionId)
        applicationEventPublisher.publishEvent(matomoEvent)
    }

    private fun setMainViewLocalization(): Pair<String, WidgetProperties.MainView.Localization> {
        return "localization" to widgetProperties.mainView.localization
    }
    private fun setMainViewMobileURL(): Pair<String, WidgetProperties.MainView.MobileUrl> {
        return "mobileUrl" to widgetProperties.mainView.mobileUrl
    }

    private fun setEiDClientURL(url: String): Pair<String, String> {
        return "eidClientURL" to url
    }

    private fun isIncompatibleOSVersion(userAgent: String): Boolean {
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

    private fun handleRequestWithIncompatibleOSVersion(): Rendering {
        publishMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.loaded,
            widgetTracking.names.incompatible
        )

        val incompatibleViewConfig = mapOf(
            "localization" to widgetProperties.errorView.incompatible.localization
        )

        return Rendering
            .view(INCOMPATIBLE_PAGE)
            .model(defaultViewHeaderConfig + incompatibleViewConfig)
            .status(HttpStatus.OK)
            .build()
    }
}
