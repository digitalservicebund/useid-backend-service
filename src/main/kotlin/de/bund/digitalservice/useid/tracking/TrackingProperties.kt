package de.bund.digitalservice.useid.tracking

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "tracking")
@Validated
@Profile("!local")
class TrackingProperties {
    @NotBlank
    lateinit var matomoSiteId: String

    @NotBlank
    lateinit var matomoDomain: String
}
