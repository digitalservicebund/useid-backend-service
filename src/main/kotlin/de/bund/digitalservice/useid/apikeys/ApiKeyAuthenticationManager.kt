package de.bund.digitalservice.useid.apikeys

import de.bund.digitalservice.useid.config.TenantProperties
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/**
 * This class is responsible to validate API keys and create an authenticated [ApiKeyAuthenticationToken] in case
 * of a valid API key.
 */
@Component
class ApiKeyAuthenticationManager(val tenantProperties: TenantProperties) : AuthenticationManager {
    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication !is ApiKeyAuthenticationToken) {
            return authentication
        }
        val validTenant = tenantProperties.findByApiKey(authentication.principal)
            ?: return authentication

        return ApiKeyAuthenticationToken(validTenant.apiKey, validTenant.refreshAddress, validTenant.dataGroups, true)
    }
}
