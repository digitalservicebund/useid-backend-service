package de.bund.digitalservice.useid.eidservice

import de.governikus.autent.key.utils.exceptions.KeyStoreCreationFailedException
import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.TestPropertySource
import java.io.FileNotFoundException
import java.security.cert.CertificateException

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(properties = ["test.invalid-resource=/foobar"])
internal class EidServiceConfigIntegrationTest {

    @Autowired
    private lateinit var config: EidServiceConfig

    @Autowired
    private lateinit var eidServiceProperties: EidServiceProperties

    @Value("\${test.invalid-resource}")
    private lateinit var invalidResource: Resource

    @Test
    fun `getEidServiceWsdlUrl returns a value`() {
        assertThat(config.eidServiceWsdlUrl).isNotNull
        assertThat(config.eidServiceWsdlUrl).isNotEmpty
    }

    @Test
    fun `getEidServiceUrl returns a value`() {
        assertThat(config.eidServiceUrl).isNotNull
        assertThat(config.eidServiceUrl).isNotEmpty
    }

    @Test
    fun `getTruststore returns not null`() {
        assertThat(config.truststore).isNotNull
    }

    @Test
    fun `getXmlSignatureVerificationCertificate returns not null`() {
        assertThat(config.xmlSignatureVerificationCertificate).isNotNull
    }

    @Test
    fun `getXmlSignatureCreationKeystore returns not null`() {
        assertThat(config.xmlSignatureCreationKeystore).isNotNull
    }

    @Test
    fun `getSslKeystoreForMutualTlsAuthentication returns not null`() {
        assertThat(config.sslKeystoreForMutualTlsAuthentication).isNotNull
    }

    @Test
    fun `createKeystoreAccessor throws exception when passed a false keystore`() {
        val keystore = copyKeystore(eidServiceProperties.soapSigKeystore)
        keystore.keystore = invalidResource

        assertThatThrownBy {
            config.createKeystoreAccessor(keystore)
        }.isInstanceOf(FileNotFoundException::class.java)
    }

    @Test
    fun `createKeystoreAccessor throws exception if password is wrong`() {
        val keystore = copyKeystore(eidServiceProperties.soapSigKeystore)
        keystore.password = "wrong-password"

        assertThatThrownBy {
            config.createKeystoreAccessor(keystore)
        }.isInstanceOf(KeyStoreCreationFailedException::class.java)
    }

    @Test
    fun `readCertificate throws SslConfigException when passing a false resource path`() {
        assertThatThrownBy {
            config.readCertificate(invalidResource)
        }.isInstanceOf(SslConfigException::class.java).hasCauseInstanceOf(FileNotFoundException::class.java)
    }

    @Test
    fun `readCertificate throws SslConfigException when CertificateFactory throws error`() {
        val mockCert = mockk<Resource>()
        every { mockCert.inputStream } throws CertificateException()

        assertThatThrownBy {
            config.readCertificate(mockCert)
        }.isInstanceOf(SslConfigException::class.java)
    }

    private fun copyKeystore(keystore: EidServiceProperties.Keystore): EidServiceProperties.Keystore {
        val copy = EidServiceProperties.Keystore()
        copy.keystore = keystore.keystore
        copy.password = keystore.password
        copy.password = keystore.password
        copy.alias = keystore.alias
        copy.type = keystore.type
        return copy
    }
}
