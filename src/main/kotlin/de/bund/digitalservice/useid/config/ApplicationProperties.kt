package de.bund.digitalservice.useid.config

import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "app")
@Validated
class ApplicationProperties {
    @NotBlank
    lateinit var baseUrl: String

    var maxPercentageOfEidFailures: Int = 80

    companion object {
        const val apiVersionPrefix = "/api/v1"
    }
}
