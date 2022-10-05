package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotEmpty

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

    fun domainIsAllowed(host: String): Boolean {
        return allowedHosts.contains(host)
    }
    fun getCSPHeaderValue(host: String): String {
        return "$defaultConfig$frameAncestors $host;"
    }

    fun getDefaultCSPHeaderValue(): String {
        return "$defaultConfig$frameAncestors;"
    }
}
