package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.tracking.TrackingServiceInterface
import io.micrometer.core.annotation.Timed
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.reactive.result.view.Rendering

internal const val WIDGET_PAGE = "widget"
internal const val INCOMPATIBLE_PAGE = "incompatible"
internal const val FALLBACK_PAGE = "eID-Client"
internal const val WIDGET_START_IDENT_BTN_CLICKED = "start-ident-button-clicked"

@Controller
@Timed
class WidgetController(
    private val applicationProperties: ApplicationProperties,
    private val widgetProperties: WidgetProperties,
    private val trackingService: TrackingServiceInterface,
    private val widgetTracking: WidgetTracking
) {
    private val defaultViewHeaderConfig = mapOf(
        "baseUrl" to applicationProperties.baseUrl,
        "metaTag" to widgetProperties.metaTag
    )

    @PostMapping("/$WIDGET_START_IDENT_BTN_CLICKED")
    fun handleAppOpened(): ResponseEntity<String> {
        trackingService.sendMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.buttonPressed,
            widgetTracking.names.startIdent
        )
        return ResponseEntity.status(HttpStatus.OK).body("")
    }

    @GetMapping("/$WIDGET_PAGE")
    fun getWidgetPage(model: Model): Rendering {
        trackingService.sendMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.loaded,
            widgetTracking.names.widget
        )

        val widgetViewConfig = mapOf(
            setMainViewLocalization(),
            setMainViewMobileURL(),
            setEiDClientURL("#"),
            "isWidget" to true
        )

        return Rendering
            .view(WIDGET_PAGE)
            .model(defaultViewHeaderConfig + widgetViewConfig)
            .status(HttpStatus.OK)
            .build()
    }

    @GetMapping("/$INCOMPATIBLE_PAGE")
    fun getIncompatiblePage(model: Model): Rendering {
        trackingService.sendMatomoEvent(
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

    @GetMapping("/$FALLBACK_PAGE")
    fun getUniversalLinkFallbackPage(model: Model, serverHttpRequest: ServerHttpRequest): Rendering {
        trackingService.sendMatomoEvent(
            widgetTracking.categories.widget,
            widgetTracking.actions.loaded,
            widgetTracking.names.fallback
        )
        /*
            Documentation about the link syntax can be found in Technical Guideline TR-03124-1 – eID-Client, Part 1:
            Specifications Version 1.4 8. October 2021, Chapter 2.2 Full eID-Client
            Note: Replaced the prefix eid:// with bundesident:// to make sure only the BundesIdent app is opened
         */
        val url = "bundesident://127.0.0.1:24727/eID-Client?${serverHttpRequest.uri.rawQuery}"

        val widgetViewConfig = mapOf(
            setMainViewLocalization(),
            setMainViewMobileURL(),
            setEiDClientURL(url),
            "localizationError" to widgetProperties.errorView.fallback.localization
        )

        return Rendering
            .view(WIDGET_PAGE)
            .model(defaultViewHeaderConfig + widgetViewConfig)
            .status(HttpStatus.OK)
            .build()
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
}
