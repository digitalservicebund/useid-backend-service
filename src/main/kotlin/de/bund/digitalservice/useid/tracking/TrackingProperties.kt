package de.bund.digitalservice.useid.tracking

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Profile("!local")
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
