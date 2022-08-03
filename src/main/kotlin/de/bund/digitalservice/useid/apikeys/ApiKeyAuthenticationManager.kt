package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.config.ApiProperties
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class ApiKeyAuthenticationManager(val apiProperties: ApiProperties) : ReactiveAuthenticationManager {
    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .filter { it is ApiKeyAuthenticationToken }
            .map { it as ApiKeyAuthenticationToken }
            .map { it.principal }
            .filter { apiProperties.apiKeys.contains(it) }
            .switchIfEmpty(Mono.error(BadCredentialsException("API key invalid.")))
            .map { ApiKeyAuthenticationToken(it, true) }
    }
}
