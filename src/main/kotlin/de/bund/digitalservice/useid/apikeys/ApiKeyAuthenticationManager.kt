package de.bund.digitalservice.useid.apikeys

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/**
 * This class is responsible to validate API keys and create an authenticated [ApiKeyAuthenticationToken] in case
 * of a valid API key.
 */
@Component
class ApiKeyAuthenticationManager(val apiProperties: ApiProperties) : AuthenticationManager {
    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication !is ApiKeyAuthenticationToken) {
            throw BadCredentialsException("API key invalid.")
        }
        val validApiKey = apiProperties.apiKeys.find { it.keyValue == authentication.principal }
            ?: throw BadCredentialsException("API key invalid.")

        return ApiKeyAuthenticationToken(validApiKey.keyValue, validApiKey.refreshAddress, validApiKey.dataGroups, true)
    }
}
