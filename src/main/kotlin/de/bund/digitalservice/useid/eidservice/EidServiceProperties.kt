package de.bund.digitalservice.useid.eidservice

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "eidservice")
@Validated
class EidServiceProperties {
    @NotBlank
    lateinit var keystorePassword: String

    @NotBlank
    lateinit var wsdlUrl: String

    @NotBlank
    lateinit var url: String

    @NotBlank
    lateinit var tlsPath: String

    @NotBlank
    lateinit var sigPath: String

    @Valid
    var tlsKeystore: TLSKeystore = TLSKeystore()

    @Valid
    var xmlSigKeystore: XMLSigKeystore = XMLSigKeystore()

    class TLSKeystore {
        @NotBlank
        lateinit var path: String

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
        @NotBlank
        lateinit var path: String

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
