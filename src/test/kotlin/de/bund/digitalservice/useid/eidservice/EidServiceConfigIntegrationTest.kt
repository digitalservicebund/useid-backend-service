package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import de.governikus.autent.key.utils.exceptions.KeyStoreCreationFailedException
import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import io.mockk.every
import io.mockk.mockk
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.Resource
import org.springframework.test.context.TestPropertySource
import java.io.FileNotFoundException
import java.security.cert.CertificateException

@TestPropertySource(properties = ["test.invalid-resource=/foobar"])
internal class EidServiceConfigIntegrationTest : PostgresTestcontainerIntegrationTest() {

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
    fun `createKeystoreAccessor throws exception when passed a false keystore`() {
        val keystore = copyKeystore(eidServiceProperties.xmlSigKeystore)
        keystore.keystore = invalidResource

        Assertions.assertThrows(FileNotFoundException::class.java) {
            config.createKeystoreAccessor(keystore)
        }
    }

    @Test
    fun `createKeystoreAccessor throws exception if password is wrong`() {
        val keystore = copyKeystore(eidServiceProperties.xmlSigKeystore)
        keystore.password = "wrong-password"

        Assertions.assertThrows(KeyStoreCreationFailedException::class.java) {
            config.createKeystoreAccessor(keystore)
        }
    }

    @Test
    fun `readCertificate throws SslConfigException when passing a false resource path`() {
        val exception = Assertions.assertThrows(SslConfigException::class.java) {
            config.readCertificate(invalidResource)
        }

        assertThat(exception.cause, Matchers.instanceOf(FileNotFoundException::class.java))
    }

    @Test
    fun `readCertificate throws SslConfigException when CertificateFactory throws error`() {
        val mockCert = mockk<Resource>()
        every { mockCert.inputStream } throws CertificateException()

        Assertions.assertThrows(SslConfigException::class.java) {
            config.readCertificate(mockCert)
        }
    }

    private fun copyKeystore(keystore: EidServiceProperties.Keystore): EidServiceProperties.Keystore {
        val copy = EidServiceProperties.Keystore()
        copy.keystore = keystore.keystore
        copy.password = keystore.password
        copy.keyPassword = keystore.keyPassword
        copy.alias = keystore.alias
        copy.type = keystore.type
        return copy
    }
}
