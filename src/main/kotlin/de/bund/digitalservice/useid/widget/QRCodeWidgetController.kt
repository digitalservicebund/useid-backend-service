// PROTOTYPE FILE

package de.bund.digitalservice.useid.widget

import io.micrometer.core.annotation.Timed
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.servlet.ModelAndView
import java.util.UUID

internal const val QRCODE_WIDGET_PAGE = "qrcode-widget"
internal const val APP_SIMULATOR_PAGE = "app-simulator"

@Controller
@Timed
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class QRCodeWidgetController {
    @GetMapping("/$QRCODE_WIDGET_PAGE")
    fun getQRCodeWidgetPage(
        model: Model,
        @RequestParam hostname: String,
        @RequestParam(required = false, name = "hash") sessionHash: String?,
    ): ModelAndView {
        // no tracking for desktop solution while prototyping

        return ModelAndView(QRCODE_WIDGET_PAGE)
    }

    @GetMapping("/$APP_SIMULATOR_PAGE")
    fun getAppSimulatorPage(
        model: Model,
        @RequestParam widgetSessionId: UUID,
    ): ModelAndView {
        // no tracking for desktop solution while prototyping

        val modelAndView = ModelAndView(APP_SIMULATOR_PAGE)
        modelAndView.addObject("widgetSessionId" to widgetSessionId)
        return modelAndView
    }
}
