package de.bund.digitalservice.useid.panstar

import de.governikus.panstar.sdk.soap.configuration.SoapKeyMaterial
import de.governikus.panstar.sdk.utils.crypto.KeystoreLoader
import org.slf4j.LoggerFactory
import org.springframework.core.io.Resource
import org.springframework.stereotype.Component
import java.io.IOException
import java.security.PrivateKey
import java.security.cert.X509Certificate

@Component
class SoapKeyMaterial(private val panstarProperties: PanstarProperties) :
    SoapKeyMaterial {
    override fun getRequestSignatureKey(): PrivateKey {
        // To load the private key we use a utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a private key.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        return loadPrivateKeyFromKeystore(panstarProperties.soapSigKeystore)
    }

    override fun getRequestSignatureCertificate(): X509Certificate {
        // To load the certificate we use a utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a certificate.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        return loadCertificateFromKeystore(panstarProperties.soapSigKeystore)
    }

    override fun getResponseSignatureValidationCertificate(): X509Certificate {
        // To load the certificate key we use a utility method from the panstar-sdk. Feel free to use your own
        // implementation or another library to load a certificate.
        // The certificate that is used can be found in the directory soap-sample/src/main/resources/keys
        return loadCertificate(panstarProperties.sigCert)
    }

    override fun getTlsClientKey(): PrivateKey {
        // To load the private key we use an utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a private key.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        return loadPrivateKeyFromKeystore(panstarProperties.soapTlsKeystore)
    }

    override fun getTlsClientCertificate(): X509Certificate {
        // To load the certificate we use an utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a certificate.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        return loadCertificateFromKeystore(panstarProperties.soapTlsKeystore)
    }

    override fun getTlsServerCertificate(): X509Certificate {
        // To load the certificate we use an utility method from the panstar-sdk. Feel free to use your own
        // implementation or another library to load a certificate.
        // The certificate that is used can be found in the directory soap-sample/src/main/resources/keys
        return loadCertificate(panstarProperties.tlsCert)
    }

    private fun loadPrivateKeyFromKeystore(keystore: PanstarProperties.Keystore) = try {
        KeystoreLoader.loadPrivateKeyFromKeystore(
            keystore.keystore.inputStream,
            keystore.type,
            keystore.password,
            keystore.alias,
            keystore.password,
        )
            .orElseThrow { IllegalStateException() }
    } catch (e: IOException) {
        log.warn("No signature key present")
        throw IllegalStateException(
            "No signature key present. Without a signature key, no calls to the SOAP service can be made.",
            e,
        )
    }

    private fun loadCertificateFromKeystore(keystore: PanstarProperties.Keystore) = try {
        KeystoreLoader.loadX509CertificateFromKeystore(
            keystore.keystore.inputStream,
            KeystoreLoader.PKCS_12,
            keystore.password,
            keystore.alias,
        )
            .orElseThrow { IllegalStateException("Failed to load certificate from keystore file " + keystore.keystore.filename) }
    } catch (e: IOException) {
        log.warn("No signature certificate present")
        throw IllegalStateException(
            "No signature certificate present. Without a signature certificate, no calls to the SOAP service can be made.",
            e,
        )
    }

    private fun loadCertificate(resource: Resource) = try {
        KeystoreLoader.loadX509Certificate(resource.inputStream)
            .orElseThrow { IllegalStateException("Failed to load certificate from file " + resource.filename) }
    } catch (e: IOException) {
        log.warn("No certificate present to verify SOAP signatures")
        throw IllegalStateException(
            "No certificate present to verify signature in SOAP response. Without a certificate to validate the signature the SOAP responses cannot be used",
            e,
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(SoapKeyMaterial::class.java)
    }
}
