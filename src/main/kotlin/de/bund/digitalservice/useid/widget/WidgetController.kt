package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ContentSecurityPolicyProperties
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class WidgetController(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) {
    @GetMapping("/widget")
    fun widget(serverHttpResponse: ServerHttpResponse, serverHttpRequest: ServerHttpRequest): Mono<String> {
        val host = serverHttpRequest.uri.host
        val allowedHost = contentSecurityPolicyProperties.domainIsAllowed(host)

        if (allowedHost) {
            serverHttpResponse.headers.set(
                "Content-Security-Policy",
                contentSecurityPolicyProperties.getCSPHeaderValue(host)
            )
        }
        return Mono.just("widget")
    }

    @GetMapping("/incompatible")
    fun noSupport(): Mono<String> = Mono.just("incompatible")
}
