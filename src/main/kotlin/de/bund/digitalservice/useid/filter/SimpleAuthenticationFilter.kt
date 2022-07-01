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

        return when {
            isValidHeader(authorizationHeader) -> webFilterchain.filter(serverWebExchange)
            else -> Mono.error(ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing: Invalid header"))
        }
    }
    private fun isValidHeader(authHeader: String?): Boolean {
        val token = "ShouldAvailableAsEnvVar"
        return authHeader != null && authHeader.contentEquals("Bearer $token")
    }
}
