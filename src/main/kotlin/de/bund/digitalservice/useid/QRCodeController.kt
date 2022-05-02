package de.bund.digitalservice.useid

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping

@Controller
class QRCodeController {
    @GetMapping("/download")
    fun downloadCode(model: Model): String = "downloadCode"

    @GetMapping("/authentication")
    fun authenticationCode(model: Model): String = "authenticationCode"
}
