package de.bund.digitalservice.useid.filter

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono

class SimpleAuthenticationFilter : WebFilter {
    override fun filter(serverWebExchange: ServerWebExchange, webFilterchain: WebFilterChain): Mono<Void> {
        val authorizationHeader: String? = serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION)

        // TODO: Is there another way writing this code below more reactive?
        return when {
            !isValidHeader(authorizationHeader) -> Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing: Invalid header"))
            else -> webFilterchain.filter(serverWebExchange)
        }
    }
    private fun isValidHeader(authHeader: String?): Boolean {
        return authHeader != null &&
            authHeader.startsWith(prefix = "Bearer ") &&
            authHeader.endsWith("ShouldAvailableAsEnvVar", ignoreCase = false)
    }
}
