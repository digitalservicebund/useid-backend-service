package de.bund.digitalservice.useid.tenant

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "tenant")
@Validated
class TenantProperties {

    var tenants: List<Tenant> = emptyList()

    fun findByApiKey(key: String?): Tenant? {
        return tenants.find { it.apiKey == key }
    }

    fun findByAllowedHost(host: String?): Tenant? {
        return tenants.find { it.allowedHost == host }
    }

    fun findByTenantId(id: String?): Tenant? {
        return tenants.find { it.id == id }
    }
}
