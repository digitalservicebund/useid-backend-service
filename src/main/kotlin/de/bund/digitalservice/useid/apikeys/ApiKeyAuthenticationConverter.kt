package de.bund.digitalservice.useid.apikeys

import org.springframework.http.HttpHeaders
import org.springframework.security.core.Authentication
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

/**
 * This class is responsible to extract an API key from incoming HTTP requests and create an unauthenticated
 * [ApiKeyAuthenticationToken] if present. The actual validation of the API key happens in the
 * [ApiKeyAuthenticationManager].
 */
@Component
class ApiKeyAuthenticationConverter : ServerAuthenticationConverter {
    override fun convert(exchange: ServerWebExchange): Mono<Authentication> {
        return Mono.just(exchange)
            .flatMap { serverWebExchange: ServerWebExchange ->
                Mono.justOrEmpty(serverWebExchange.request.headers.getFirst(HttpHeaders.AUTHORIZATION))
            }
            .filter { header: String ->
                header.trim().isNotEmpty() && header.trim().startsWith(AUTH_HEADER_VALUE_PREFIX)
            }
            .map { header: String ->
                header.substring(AUTH_HEADER_VALUE_PREFIX.length)
            }
            .switchIfEmpty(Mono.empty())
            .map { token: String ->
                ApiKeyAuthenticationToken(token)
            }
    }
}
