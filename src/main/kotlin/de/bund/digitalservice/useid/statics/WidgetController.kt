package de.bund.digitalservice.useid.statics

import de.bund.digitalservice.useid.config.ContentSecurityPolicyProperties
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class WidgetController(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) {
    @GetMapping("/widget")
    fun widget(mode: Model, response: ServerHttpResponse, serverHttpRequest: ServerHttpRequest): Mono<String> {
        val host = serverHttpRequest.uri.host
        val allowedHost = contentSecurityPolicyProperties.domainIsAllowed(host)

        if (allowedHost) {
            response.headers.set(
                "Content-Security-Policy",
                contentSecurityPolicyProperties.getCSPHeaderValue(host)
            )
        }
        return Mono.just("widget")
    }

    @GetMapping("/incompatible")
    fun noSupport(model: Model): Mono<String> = Mono.just("incompatible")
}
