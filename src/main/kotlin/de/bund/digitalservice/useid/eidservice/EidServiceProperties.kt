package de.bund.digitalservice.useid.eidservice

import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated

@Component
@ConfigurationProperties(prefix = "eidservice")
@Validated
class EidServiceProperties {
    @NotBlank
    lateinit var truststorePassword: String

    @NotBlank
    lateinit var wsdlUrl: String

    @NotBlank
    lateinit var url: String

    lateinit var tlsCert: Resource

    lateinit var sigCert: Resource

    @Valid
    var soapTlsKeystore: Keystore = Keystore()

    @Valid
    var soapSigKeystore: Keystore = Keystore()

    @NotNull
    var connectTimeoutInMillis: Int? = null

    @NotNull
    var readTimeoutInMillis: Int? = null

    class Keystore {
        lateinit var keystore: Resource

        @NotBlank
        lateinit var type: String

        @NotBlank
        lateinit var alias: String

        @NotBlank
        lateinit var password: String
    }
}
