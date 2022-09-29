package de.bund.digitalservice.useid.widget

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
@Timed
class WidgetController {
    @GetMapping("/widget")
    fun widget(model: Model): Mono<String> = Mono.just("widget")

    @GetMapping("/incompatible")
    fun noSupport(model: Model): Mono<String> = Mono.just("incompatible")
}
