package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.config.ApiProperties
import mu.KotlinLogging
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

/**
 * This class is responsible to validate API keys and create an authenticated [ApiKeyAuthenticationToken] in case
 * of a valid API key.
 */
@Component
class ApiKeyAuthenticationManager(val apiProperties: ApiProperties) : ReactiveAuthenticationManager {
    private val log = KotlinLogging.logger {}

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        return Mono.just(authentication)
            .filter { it is ApiKeyAuthenticationToken }
            .map { it as ApiKeyAuthenticationToken }
            .map { it.principal }
            .mapNotNull { keyValue ->
                val validApiKey = apiProperties.apiKeys.find { it.keyValue == keyValue }
                if (validApiKey == null) {
                    log.debug { "Invalid API key." }
                }
                validApiKey
            }
            .switchIfEmpty(Mono.error(BadCredentialsException("API key invalid.")))
            .map { ApiKeyAuthenticationToken(it!!.keyValue, it.refreshAddress, true) }
    }
}
