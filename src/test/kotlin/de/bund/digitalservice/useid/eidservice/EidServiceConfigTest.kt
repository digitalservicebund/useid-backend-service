package de.bund.digitalservice.useid.eidservice

import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.TestPropertySource
import java.io.FileNotFoundException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@TestPropertySource(properties = ["test.invalid-resource=/foobar"])
internal class EidServiceConfigTest {

    @Autowired
    private lateinit var config: EidServiceConfig

    @Autowired
    private lateinit var eidServiceProperties: EidServiceProperties

    @Value("\${test.invalid-resource}")
    private lateinit var invalidResource: Resource

    @Test
    fun `getEidServiceWsdlUrl returns a value`() {
        assertNotNull(config.eidServiceWsdlUrl)
        assertNotEquals("", config.eidServiceWsdlUrl)
    }

    @Test
    fun `getEidServiceUrl returns a value`() {
        assertNotNull(config.eidServiceUrl)
        assertNotEquals("", config.eidServiceUrl)
    }

    @Test
    fun `getTruststore returns not null`() {
        assertNotNull(config.truststore)
    }

    @Test
    fun `getXmlSignatureVerificationCertificate returns not null`() {
        assertNotNull(config.xmlSignatureVerificationCertificate)
    }

    @Test
    fun `getXmlSignatureCreationKeystore returns not null`() {
        assertNotNull(config.xmlSignatureCreationKeystore)
    }

    @Test
    fun `getSslKeystoreForMutualTlsAuthentication returns not null`() {
        assertNotNull(config.sslKeystoreForMutualTlsAuthentication)
    }

    @Test
    fun `createKeystoreAccessor throws error when passed a false keystore`() {
        val keystore = eidServiceProperties.xmlSigKeystore
        keystore.keystore = invalidResource

        Assertions.assertThrows(FileNotFoundException::class.java) {
            config.createKeystoreAccessor(keystore)
        }
    }

    @Test
    fun `readCertificate fails when passing a false resource path`() {
        val exception = Assertions.assertThrows(SslConfigException::class.java) {
            config.readCertificate(invalidResource)
        }

        assertThat(exception.cause, Matchers.instanceOf(FileNotFoundException::class.java))
    }
}
