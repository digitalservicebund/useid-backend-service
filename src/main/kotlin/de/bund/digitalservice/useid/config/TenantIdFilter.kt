package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

@Component
class TenantIdFilter : WebFilter {
    override fun filter(
        serverWebExchange: ServerWebExchange,
        webFilterChain: WebFilterChain
    ): Mono<Void> {
        serverWebExchange.attributes.put("tenantId", "unknown")
        return webFilterChain.filter(serverWebExchange)
    }
}
