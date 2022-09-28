package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "csp")
@Validated
class ContentSecurityPolicyProperties {
    @NotBlank
    lateinit var defaultConfig: String

    @NotBlank
    lateinit var frameAncestors: String
}
