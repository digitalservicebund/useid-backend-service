package de.bund.digitalservice.useid

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class QRCodeController {
    @GetMapping("/widget")
    fun widget(model: Model): String = "widget"
}
