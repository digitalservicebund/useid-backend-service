package de.bund.digitalservice.useid.tracking

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

// @Profile("!local")
@Component
@ConfigurationProperties(prefix = "tracking")
@Validated
class TrackingProperties {

    @Valid
    var matomo: Matomo = Matomo()

    class Matomo {
        @NotBlank
        lateinit var siteId: String

        @NotBlank
        lateinit var domain: String
    }
}
