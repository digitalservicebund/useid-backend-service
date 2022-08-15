package de.bund.digitalservice.useid.eidservice

import de.governikus.autent.key.utils.KeyStoreSupporter
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import de.governikus.autent.sdk.eidservice.wrapper.KeyStoreAccessor
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.xml.ws.BindingProvider

@Configuration
class EidServiceConfig(private var eidAutentProperties: EidAutentProperties) : EidServiceConfiguration {
    override fun getEidServiceWsdlUrl(): String = eidAutentProperties.eidservice.wsdlUrl

    override fun getEidServiceUrl(): String = eidAutentProperties.eidservice.url

    override fun getTruststore(): KeyStore {
        return KeyStoreSupporter.toKeyStore(
            readCertificate(eidAutentProperties.eidservice.tlsPath),
            "eid",
            eidAutentProperties.keystorePassword,
            KeyStoreSupporter.KeyStoreType.JKS
        )
    }

    override fun getXmlSignatureVerificationCertificate(): X509Certificate = readCertificate(eidAutentProperties.eidservice.sigPath)
    override fun getXmlSignatureCreationKeystore(): KeyStoreAccessor {
        val sigKeyStore = KeyStoreSupporter.readKeyStore(
            ClassPathResource(eidAutentProperties.xmlSigKeystore.path).inputStream,
            KeyStoreSupporter.KeyStoreType.valueOf(eidAutentProperties.xmlSigKeystore.type),
            eidAutentProperties.xmlSigKeystore.password
        )
        return KeyStoreAccessor(
            sigKeyStore,
            eidAutentProperties.xmlSigKeystore.password,
            eidAutentProperties.xmlSigKeystore.alias,
            eidAutentProperties.xmlSigKeystore.keyPassword
        )
    }

    override fun getSslKeystoreForMutualTlsAuthentication(): KeyStoreAccessor {
        val tlsKeyStore = KeyStoreSupporter.readKeyStore(
            ClassPathResource(eidAutentProperties.tlsKeystore.path).inputStream,
            KeyStoreSupporter.KeyStoreType.valueOf(eidAutentProperties.tlsKeystore.type),
            eidAutentProperties.tlsKeystore.password
        )
        return KeyStoreAccessor(
            tlsKeyStore,
            eidAutentProperties.tlsKeystore.password,
            eidAutentProperties.tlsKeystore.alias,
            eidAutentProperties.tlsKeystore.keyPassword
        )
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
    private fun readCertificate(path: String): X509Certificate {
        try {
            val certificateResource = ClassPathResource(path)
            val certFactory = CertificateFactory.getInstance("X.509")

            return certificateResource.inputStream.use {
                certFactory.generateCertificate(it) as X509Certificate
            }
        } catch (e: CertificateException) {
            throw SslConfigException(e)
        } catch (e: IOException) {
            throw SslConfigException(e)
        }
    }
}
