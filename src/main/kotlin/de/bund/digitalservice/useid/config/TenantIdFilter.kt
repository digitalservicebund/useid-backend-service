package de.bund.digitalservice.useid.config

import org.springframework.security.core.context.SecurityContextHolder
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
        // Retrieve tenant id either from authenticated security context (api keys) or from url.
        // see the other two requests filters for more information
        // We still need a data structure that allows to retrieve the tenant id for a given value
        //
        // For now we always set the tenant id to unknown
        val tenantId: String
        val isAuthenticated : Boolean? = SecurityContextHolder.getContext()?.authentication?.isAuthenticated

        tenantId = if (isAuthenticated == true) {
            // api call with authentication token
            "unknown-api-call"
        } else {
            // not an authenticated call or a failed authentication call
            // get tenant id based on csp header?
            "client-call"
        }

        serverWebExchange.attributes["tenantId"] = tenantId

        return webFilterChain.filter(serverWebExchange)
    }
}
