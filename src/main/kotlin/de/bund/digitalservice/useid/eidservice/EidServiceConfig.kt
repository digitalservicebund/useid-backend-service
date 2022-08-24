package de.bund.digitalservice.useid.eidservice

import de.governikus.autent.key.utils.KeyStoreSupporter
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import de.governikus.autent.sdk.eidservice.wrapper.KeyStoreAccessor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.Resource
import java.io.IOException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.xml.ws.BindingProvider

@Configuration
@ConditionalOnProperty("feature.eid-service-integration.enabled", havingValue = "true")
class EidServiceConfig(private var eidServiceProperties: EidServiceProperties) : EidServiceConfiguration {
    override fun getEidServiceWsdlUrl(): String = eidServiceProperties.wsdlUrl

    override fun getEidServiceUrl(): String = eidServiceProperties.url

    override fun getTruststore(): KeyStore {
        return KeyStoreSupporter.toKeyStore(
            readCertificate(eidServiceProperties.tlsCert),
            "eid",
            eidServiceProperties.keystorePassword,
            KeyStoreSupporter.KeyStoreType.JKS
        )
    }

    override fun getXmlSignatureVerificationCertificate(): X509Certificate = readCertificate(eidServiceProperties.sigCert)
    override fun getXmlSignatureCreationKeystore(): KeyStoreAccessor {
        return createKeystoreAccessor(eidServiceProperties.xmlSigKeystore)
    }

    override fun getSslKeystoreForMutualTlsAuthentication(): KeyStoreAccessor {
        return createKeystoreAccessor(eidServiceProperties.tlsKeystore)
    }

    fun createKeystoreAccessor(keystore: EidServiceProperties.Keystore): KeyStoreAccessor {
        val tlsKeyStore = KeyStoreSupporter.readKeyStore(
            keystore.keystore.inputStream,
            KeyStoreSupporter.KeyStoreType.valueOf(keystore.type),
            keystore.password
        )
        return KeyStoreAccessor(tlsKeyStore, keystore.password, keystore.alias, keystore.keyPassword)
    }

    override fun configureEidPort(eidPort: BindingProvider) {
        // No implementation since this method is an abstract method
    }

    /**
     * Read a certificate from the classpath.
     *
     * @param path classpath to the certificate
     * @return the certificate
     */
    fun readCertificate(cert: Resource): X509Certificate {
        try {
            val certFactory = CertificateFactory.getInstance("X.509")

            return cert.inputStream.use {
                certFactory.generateCertificate(it) as X509Certificate
            }
        } catch (e: CertificateException) {
            throw SslConfigException(e)
        } catch (e: IOException) {
            throw SslConfigException(e)
        }
    }
}
