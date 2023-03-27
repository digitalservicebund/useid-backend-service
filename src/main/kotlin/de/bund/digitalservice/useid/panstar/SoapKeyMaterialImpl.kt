package de.bund.digitalservice.useid.panstar

import de.governikus.panstar.sdk.soap.configuration.SoapKeyMaterial
import de.governikus.panstar.sdk.utils.crypto.KeystoreLoader
import org.slf4j.LoggerFactory
import org.springframework.core.io.ClassPathResource
import org.springframework.stereotype.Component
import java.io.IOException
import java.security.PrivateKey
import java.security.cert.X509Certificate

@Component
class SoapKeyMaterialImpl : SoapKeyMaterial {
    override fun getRequestSignatureKey(): PrivateKey {
        // To load the private key we use a utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a private key.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/Governikus_GmbH_&_Co._KG_Localhost_SAML_Signature_620935.p12")
        return try {
            KeystoreLoader.loadPrivateKeyFromKeystore(
                classPathResource.inputStream,
                KeystoreLoader.PKCS_12,
                "620935",
                "saml-signature",
                "620935",
            )
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No signature key present")
            throw IllegalStateException(
                "No signature key present. Without a signature key, no calls to the SOAP service can be made.",
                e,
            )
        }
    }

    override fun getRequestSignatureCertificate(): X509Certificate {
        // To load the certificate we use a utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a certificate.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/Governikus_GmbH_&_Co._KG_Localhost_SAML_Signature_620935.p12")
        return try {
            KeystoreLoader.loadX509CertificateFromKeystore(
                classPathResource.inputStream,
                KeystoreLoader.PKCS_12,
                "620935",
                "saml-signature",
            )
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No signature certificate present")
            throw IllegalStateException(
                "No signature certificate present. Without a signature certificate, no calls to the SOAP service can be made.",
                e,
            )
        }
    }

    override fun getResponseSignatureValidationCertificate(): X509Certificate {
        // To load the certificate key we use an utility method from the panstar-sdk. Feel free to use your own
        // implementation or another library to load a certificate.
        // The certificate that is used can be found in the directory soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/dev.id.governikus-eid.de-ws-sig.cer")
        return try {
            KeystoreLoader.loadX509Certificate(classPathResource.inputStream)
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No certificate present to verify SOAP signatures")
            throw IllegalStateException(
                "No certificate present to verify signature in SOAP response. Without a certificate to validate the signature the SOAP responses cannot be used",
                e,
            )
        }
    }

    override fun getTlsClientKey(): PrivateKey {
        // To load the private key we use an utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a private key.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/mTLS.jks")
        return try {
            KeystoreLoader.loadPrivateKeyFromKeystore(
                classPathResource.inputStream,
                KeystoreLoader.JKS,
                "123456",
                "panstar sdk sample soap mtls",
                "123456",
            )
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No TLS client key present")
            throw IllegalStateException(
                "No TLS client key present. Without a TLS client key, no connection to the SOAP service can be made.",
                e,
            )
        }
    }

    override fun getTlsClientCertificate(): X509Certificate {
        // To load the certificate we use an utility method from the panstar-sdk. Feel free to use your own implementation
        // or another library to load a certificate.
        // The key store that is used can be found in the directory /soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/mTLS.jks")
        return try {
            KeystoreLoader.loadX509CertificateFromKeystore(
                classPathResource.inputStream,
                KeystoreLoader.PKCS_12,
                "123456",
                "panstar sdk sample soap mtls",
            )
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No TLS client certificate present")
            throw IllegalStateException(
                "No TLS client certificate present. Without a TLS client certificate, no connection to the SOAP service can be made.",
                e,
            )
        }
    }

    override fun getTlsServerCertificate(): X509Certificate {
        // To load the certificate we use an utility method from the panstar-sdk. Feel free to use your own
        // implementation or another library to load a certificate.
        // The certificate that is used can be found in the directory soap-sample/src/main/resources/keys
        val classPathResource = ClassPathResource("keys/dev.id.governikus-eid.de.cer")
        return try {
            KeystoreLoader.loadX509Certificate(classPathResource.inputStream)
                .orElseThrow { IllegalStateException() }
        } catch (e: IOException) {
            LOG.warn("No TLS server certificate present")
            throw IllegalStateException(
                "No TLS server certificate present. Without a TLS server certificate, no connection to the SOAP service can be made.",
                e,
            )
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(SoapKeyMaterialImpl::class.java)
    }
}
