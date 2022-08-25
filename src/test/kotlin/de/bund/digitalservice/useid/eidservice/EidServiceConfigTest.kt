package de.bund.digitalservice.useid.eidservice

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.Resource
import org.springframework.test.context.TestPropertySource
import java.io.FileNotFoundException
import java.security.KeyStore

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
        assertNotEquals("", config.eidServiceWsdlUrl)
        assertNotEquals(null, config.eidServiceWsdlUrl)
    }

    @Test
    fun `getEidServiceUrl returns a value`() {
        assertNotEquals("", config.eidServiceUrl)
        assertNotEquals(null, config.eidServiceUrl)
    }

    @Test
    fun `getTruststore returns not null`() {
        assertNotEquals(null, config.truststore)
        assert(config.truststore is KeyStore) // useless?
    }

    @Test
    fun `getXmlSignatureVerificationCertificate returns not null`() {
        assertNotEquals(null, config.xmlSignatureVerificationCertificate)
    }

    // not working so far
    @Test
    fun `getXmlSignatureCreationKeystore returns not null`() {
        assertNotEquals(null, config.xmlSignatureCreationKeystore)
    }
    //
    // @Test
    // fun `getSslKeystoreForMutualTlsAuthentication returns not null`() {
    //     assertNotEquals(null, config.sslKeystoreForMutualTlsAuthentication);
    // }

    @Test
    fun `createKeystoreAccessor throws error when passed a false keystore`() {
        val keystore = eidServiceProperties.xmlSigKeystore
        keystore.keystore = invalidResource

        Assertions.assertThrows(FileNotFoundException::class.java) {
            config.createKeystoreAccessor(keystore)
        }

        // config.createKeystoreAccessor(keystore)

        // all things are not working...
        // been following this one: https://www.baeldung.com/kotlin/assertfailswith

        // assertThrows(KeyStoreCreationFailedException, config.createKeystoreAccessor(keystore))

        // assertThrows<KeyStoreCreationFailedException> {
        //     config.createKeystoreAccessor(keystore)
        // }

        // assertFailsWith<KeyStoreCreationFailedException> {
        //     config.createKeystoreAccessor(keystore)
        // }

        // assertFailsWith(null, config.createKeystoreAccessor(keystore));
        // assertFailsWith<de.governikus.autent.key.utils.exceptions.KeyStoreCreationFailedException>
    }

    @Test
    fun `readCertificate fails when passing a false resource path`() {
        // TODO: TEST EXCEPTIONS HERE AS WELL
    }
}
