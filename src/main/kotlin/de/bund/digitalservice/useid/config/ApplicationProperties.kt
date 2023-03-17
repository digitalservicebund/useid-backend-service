package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "app")
@Validated
class ApplicationProperties {
    @NotBlank
    lateinit var baseUrl: String
}
