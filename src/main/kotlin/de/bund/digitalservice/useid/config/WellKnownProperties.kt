package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotEmpty

@Component
@ConfigurationProperties(prefix = "wellknown")
@Validated
class WellKnownProperties {
    @Valid
    var iosConfig: IOSConfig = IOSConfig()

    @Valid
    var androidConfig: AndroidConfig = AndroidConfig()

    class IOSConfig {
        @NotEmpty
        lateinit var appId: String

        @NotEmpty
        lateinit var appIdPreview: String

        @NotEmpty
        lateinit var pathUrl: String
    }

    class AndroidConfig {
        @NotEmpty
        lateinit var relation: String

        @NotEmpty
        lateinit var namespace: String

        @Valid
        var packageDefault: FingerprintProperties = FingerprintProperties()

        @Valid
        var packagePreview: FingerprintProperties = FingerprintProperties()

        class FingerprintProperties {
            @NotEmpty
            lateinit var name: String

            @NotEmpty
            lateinit var fingerprint: String
        }
    }
}
