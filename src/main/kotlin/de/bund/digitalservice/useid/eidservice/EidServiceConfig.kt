package de.bund.digitalservice.useid.eidservice

import de.governikus.autent.key.utils.KeyStoreSupporter
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.exceptions.SslConfigException
import de.governikus.autent.sdk.eidservice.wrapper.KeyStoreAccessor
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.IOException
import java.security.KeyStore
import java.security.cert.CertificateException
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate
import javax.xml.ws.BindingProvider

@Configuration
class EidServiceConfig : EidServiceConfiguration {
    /**
     * This value holds the URL where to download the WSDL.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${autent.eidservice.wsdl.url}")
    private lateinit var wsdlUrl: String

    /**
     * This value holds the URL of the eID service.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${autent.eidservice.url}")
    private lateinit var serviceUrl: String

    /**
     * This value holds the path to the TLS certificate of the Autent server.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${autent.eidservice.tls.path}")
    private lateinit var autentTlsCertPath: String

    /**
     * This value holds the password to the TLS certificate keystore of the Autent server.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${autent.keystore.password}")
    private lateinit var autentTlsCertPassword: String

    /**
     * This value holds the path to the keystore that should be used for the TLS client authentication.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${tls.keystore.path}")
    private lateinit var tlsKeystorePath: String

    /**
     * This value holds the type of the keystore that should be used for the TLS client authentication. Possible
     * values: `JKS` and `PKCS12`.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${tls.keystore.type}")
    private lateinit var tlsKeystoreType: String

    /**
     * This value holds the alias of the key that should be used for the TLS client authentication.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${tls.keystore.alias}")
    private lateinit var tlsKeystoreAlias: String

    /**
     * This value holds the password to access the keystore that should be used for the TLS client
     * authentication.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${tls.keystore.password}")
    private lateinit var tlsKeystorePassword: String

    /**
     * This value holds the password to access the key for alias [.sigKeystoreAlias] that should be used
     * for the TLS client authentication.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${tls.keystore.key.password}")
    private lateinit var tlsKeystoreKeyPassword: String

    /**
     * This value holds the path to the signature certificate of the Autent server.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${autent.eidservice.sig.path}")
    private lateinit var autentSigCertPath: String

    /**
     * This value holds the path to the keystore that should be used for the XML signature.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${xmlsig.keystore.path}")
    private lateinit var xmlSignatureKeystorePath: String

    /**
     * This value holds the type of the keystore that should be used for the XML signature. Possible values:
     * `JKS` and `PKCS12`.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${xmlsig.keystore.type}")
    private lateinit var sigKeystoreType: String

    /**
     * This value holds the alias of the key that should be used for the XML signature.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${xmlsig.keystore.alias}")
    private lateinit var sigKeystoreAlias: String

    /**
     * This value holds the password to access the keystore that should be used for the XML signature.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${xmlsig.keystore.password}")
    private lateinit var sigKeystorePassword: String

    /**
     * This value holds the password to access the key for alias [.sigKeystoreAlias] that should be used
     * for the XML signature.
     *
     * @see "classpath:application.yaml"
     */
    @Value("\${xmlsig.keystore.key.password}")
    private lateinit var sigKeystoreKeyPassword: String

    override fun getEidServiceWsdlUrl(): String = wsdlUrl

    override fun getEidServiceUrl(): String = serviceUrl

    override fun getTruststore(): KeyStore {
        return KeyStoreSupporter.toKeyStore(
            readCertificate(autentTlsCertPath),
            "eid",
            autentTlsCertPassword,
            KeyStoreSupporter.KeyStoreType.JKS
        )
    }

    override fun getXmlSignatureVerificationCertificate(): X509Certificate = readCertificate(autentSigCertPath)

    override fun getXmlSignatureCreationKeystore(): KeyStoreAccessor {
        val sigKeyStore = KeyStoreSupporter.readKeyStore(
            ClassPathResource(xmlSignatureKeystorePath).inputStream,
            KeyStoreSupporter.KeyStoreType.valueOf(sigKeystoreType),
            sigKeystorePassword
        )
        return KeyStoreAccessor(sigKeyStore, sigKeystorePassword, sigKeystoreAlias, sigKeystoreKeyPassword)
    }

    override fun getSslKeystoreForMutualTlsAuthentication(): KeyStoreAccessor {
        val tlsKeyStore = KeyStoreSupporter.readKeyStore(
            ClassPathResource(tlsKeystorePath).inputStream,
            KeyStoreSupporter.KeyStoreType.valueOf(tlsKeystoreType),
            tlsKeystorePassword
        )
        return KeyStoreAccessor(tlsKeyStore, tlsKeystorePassword, tlsKeystoreAlias, tlsKeystoreKeyPassword)
    }

    override fun configureEidPort(eidPort: BindingProvider) {
        // No implementation
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
