package de.bund.digitalservice.useid.widget

import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

internal const val WIDGET_PAGE_PATH = "/widget"
internal const val INCOMPATIBLE_PAGE_PATH = "/incompatible"

@Controller
class WidgetController {
    @GetMapping(WIDGET_PAGE_PATH)
    fun widget(): Mono<String> = Mono.just("widget")

    @GetMapping(INCOMPATIBLE_PAGE_PATH)
    fun noSupport(): Mono<String> = Mono.just("incompatible")
}
