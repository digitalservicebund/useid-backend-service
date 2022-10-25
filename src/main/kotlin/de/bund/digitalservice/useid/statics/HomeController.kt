package de.bund.digitalservice.useid.statics

import io.micrometer.core.annotation.Timed
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono
import java.util.Date

@Controller
@Timed
class HomeController {
    @GetMapping("/")
    fun home(): Mono<String> = Mono.just("redirect:https://digitalservice.bund.de")

    @GetMapping("/boom")
    fun testSentryIntegration(): Mono<Void> {
        throw Exception("This is a test exception for Sentry thrown at ${Date()}")
    }
}
