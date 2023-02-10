package de.bund.digitalservice.useid.statics

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller
@Timed
class HomeController {
    @GetMapping("/")
    fun home(): String = "redirect:https://digitalservice.bund.de"
}
