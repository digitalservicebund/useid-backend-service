package de.bund.digitalservice.useid

import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class QRCodeController {
    @GetMapping("/widget")
    fun widget(model: Model): Mono<String> = Mono.just("widget")
}
