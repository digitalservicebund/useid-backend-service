package de.bund.digitalservice.useid.eidservice

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "eidservice")
@Validated
@ConditionalOnProperty("feature.eid-service-integration.enabled", havingValue = "true")
class EidServiceProperties {
    @NotBlank
    lateinit var keystorePassword: String

    @NotBlank
    lateinit var wsdlUrl: String

    @NotBlank
    lateinit var url: String

    lateinit var tlsCert: Resource

    lateinit var sigCert: Resource

    @Valid
    var tlsKeystore: TLSKeystore = TLSKeystore()

    @Valid
    var xmlSigKeystore: XMLSigKeystore = XMLSigKeystore()

    class TLSKeystore {
        lateinit var keystore: Resource

        @NotBlank
        lateinit var type: String

        @NotBlank
        lateinit var alias: String

        @NotBlank
        lateinit var password: String

        @NotBlank
        lateinit var keyPassword: String
    }

    class XMLSigKeystore {
        lateinit var keystore: Resource

        @NotBlank
        lateinit var type: String

        @NotBlank
        lateinit var alias: String

        @NotBlank
        lateinit var password: String

        @NotBlank
        lateinit var keyPassword: String
    }
}
