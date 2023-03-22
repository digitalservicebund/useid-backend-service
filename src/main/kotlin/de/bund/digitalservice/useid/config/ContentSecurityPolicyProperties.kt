package de.bund.digitalservice.useid.config

import jakarta.validation.constraints.NotBlank
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

    fun getCSPHeaderValue(host: String): String {
        return "$defaultConfig$frameAncestors $host;"
    }

    fun getDefaultCSPHeaderValue(): String {
        return "$defaultConfig$frameAncestors;"
    }
}
