package de.bund.digitalservice.useid.tenant

import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.stereotype.Component

/**
 * This class is responsible to validate API keys and create an authenticated [TenantAuthentication] in case
 * of a valid API key.
 */
@Component
class TenantAuthenticationManager(val tenantProperties: TenantProperties) : AuthenticationManager {
    override fun authenticate(authentication: Authentication): Authentication {
        if (authentication !is TenantAuthentication) {
            return authentication
        }
        val validTenant = tenantProperties.findByApiKey(authentication.details.apiKey)
            ?: return authentication

        return TenantAuthentication(validTenant, true)
    }
}
