package de.bund.digitalservice.useid.panstar

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "panstar")
@Validated
class PanstarProperties {
    @NotBlank
    lateinit var truststorePassword: String

    @NotBlank
    lateinit var url: String

    lateinit var tlsCert: Resource

    lateinit var sigCert: Resource

    @Valid
    var soapTlsKeystore: Keystore = Keystore()

    @Valid
    var soapSigKeystore: Keystore = Keystore()

    class Keystore {
        lateinit var keystore: Resource

        @NotBlank
        lateinit var alias: String

        @NotBlank
        lateinit var password: String
    }
}
