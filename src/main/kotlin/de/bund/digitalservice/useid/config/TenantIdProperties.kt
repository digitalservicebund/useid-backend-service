package de.bund.digitalservice.useid.config

import org.springframework.stereotype.Component

@Component
class TenantIdProperties {
    fun getTenantIdForHost(host: String?): String {
        return "unknown"
    }

    fun getSanitizedTenantID(tenantId: String?): String {
        return "unknown"
    }
}
