package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "tenant")
@Validated
class TenantProperties {

    var tenants: List<Tenant> = emptyList()

    class Tenant {
        lateinit var id: String

        // api key information
        lateinit var apiKey: String
        lateinit var refreshAddress: String
        var dataGroups: List<String> = emptyList()

        // csp header information
        lateinit var defaultConfig: String
        lateinit var frameAncestors: String
        lateinit var allowedHosts: List<String>

    }
}