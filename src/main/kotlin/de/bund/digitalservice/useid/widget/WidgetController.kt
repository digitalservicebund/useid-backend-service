package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ApplicationProperties
import io.micrometer.core.annotation.Timed
import org.springframework.http.HttpStatus
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.reactive.result.view.Rendering

internal const val WIDGET_PAGE = "widget"
internal const val INCOMPATIBLE_PAGE = "incompatible"
internal const val FALLBACK_PAGE = "eID-Client"

@Controller
@Timed
class WidgetController(
    private val applicationProperties: ApplicationProperties,
    private val widgetProperties: WidgetProperties
) {
    private val defaultViewHeaderConfig = mapOf(
        "baseUrl" to applicationProperties.baseUrl,
        "metaTag" to widgetProperties.metaTag
    )

    @GetMapping("/$WIDGET_PAGE")
    fun getWidgetPage(model: Model): Rendering {
        val widgetViewConfig = mapOf(
            "localization" to widgetProperties.mainView.localization,
            "mobileUrl" to widgetProperties.mainView.mobileUrl,
            "eidClientURL" to "#",
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
        /*
            Documentation about the link syntax can be found in
            Technical Guideline TR-03124-1 – eID-Client, Part 1: Specifications Version 1.4 8. October 2021
            Chapter 2.2 Full eID-Client
         */
        val url = "eid://127.0.0.1:24727/eID-Client?${serverHttpRequest.uri.rawQuery}"

        val widgetViewConfig = mapOf(
            "localization" to widgetProperties.mainView.localization,
            "localizationError" to widgetProperties.errorView.fallback.localization,
            "mobileUrl" to widgetProperties.mainView.mobileUrl,
            "eidClientURL" to url,
            "isFallback" to true
        )

        return Rendering
            .view(WIDGET_PAGE)
            .model(defaultViewHeaderConfig + widgetViewConfig)
            .status(HttpStatus.OK)
            .build()
    }
}
