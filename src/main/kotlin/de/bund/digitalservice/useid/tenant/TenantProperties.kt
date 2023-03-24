package de.bund.digitalservice.useid.tenant

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "tenant")
@Validated
class TenantProperties {

    var tenants: List<Tenant> = emptyList()

    fun findByApiKey(key: String): Tenant? {
        return tenants.find { it.apiKey == key }
    }

    fun findByAllowedHost(host: String): Tenant? {
        for (tenant in tenants) {
            for (allowedHost in tenant.allowedHosts) {
                if (allowedHost == host) return tenant
            }
        }
        return null
    }

    fun findByTenantId(id: String): Tenant? {
        return tenants.find { it.id == id }
    }
}
