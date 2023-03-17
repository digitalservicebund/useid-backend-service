package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ApplicationProperties
import io.micrometer.core.annotation.Timed
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView

internal const val QRCODE_WIDGET_PAGE = "qrcode-widget"

@Controller
@Timed
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class DesktopWidgetController(
    private val applicationProperties: ApplicationProperties,
) {
    @GetMapping("/$QRCODE_WIDGET_PAGE")
    fun getQRCodeWidgetPage(
        model: Model,
        @RequestParam hostname: String,
        @RequestParam(required = false, name = "hash") sessionHash: String?,
    ): ModelAndView {
        // no tracking for desktop solution while prototyping

        val widgetViewConfig = mapOf(
            "baseUrl" to applicationProperties.baseUrl,
            "isWidget" to true,
            "additionalClass" to "",
        )

        val modelAndView = ModelAndView(QRCODE_WIDGET_PAGE)
        modelAndView.addAllObjects(widgetViewConfig)

        return modelAndView
    }
}
