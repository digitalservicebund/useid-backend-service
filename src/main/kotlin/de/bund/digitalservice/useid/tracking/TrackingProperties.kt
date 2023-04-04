package de.bund.digitalservice.useid.tracking

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "tracking")
@Validated
@Profile("!test") // (integration) tests do not need to fire tracking events
class TrackingProperties {

    @Valid
    var matomo: Matomo = Matomo()

    class Matomo {
        @NotBlank
        lateinit var siteId: String

        @NotBlank
        lateinit var domain: String

        @NotBlank
        lateinit var dimensionIdTenant: String
    }
}
