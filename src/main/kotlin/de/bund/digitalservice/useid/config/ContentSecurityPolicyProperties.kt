package de.bund.digitalservice.useid.config

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "csp")
@Validated
class ContentSecurityPolicyProperties {
    @NotBlank
    lateinit var defaultConfig: String

    @NotBlank
    lateinit var frameAncestors: String

    @NotEmpty
    lateinit var allowedHosts: List<String>

    @NotEmpty
    lateinit var hosts: List<Map<String, String>>

    fun domainIsAllowed(host: String): Boolean {
        return hosts.find { it["host"] == host } != null || allowedHosts.contains(host)
    }
    fun getCSPHeaderValue(host: String): String {
        return "$defaultConfig$frameAncestors $host;"
    }

    fun getDefaultCSPHeaderValue(): String {
        return "$defaultConfig$frameAncestors;"
    }

    fun getTenantId(host: String): String {
        val entry = hosts.find { it["host"] == host }
        return if (entry != null) {
            (
                {
                    entry["host"]
                }
                ).toString()
        } else {
            "unknown"
        }
    }
}
