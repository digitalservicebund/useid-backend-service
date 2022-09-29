package de.bund.digitalservice.useid.statics

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
@Timed
class HomeController {
    @GetMapping("/")
    fun home(): Mono<String> = Mono.just("redirect:https://digitalservice.bund.de")
}
