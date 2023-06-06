// PROTOTYPE FILE

package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import java.util.UUID

data class Credential(
    val credentialId: UUID,
    val widgetSessionId: UUID,
    val username: String,
    val userIdBase64: String,
    val refreshAddress: String,
    val pkcCreationOptions: PublicKeyCredentialCreationOptions,
) {
    var keyId: PublicKeyCredentialDescriptor? = null
    var publicKeyCose: ByteArray? = null
    var isDiscoverable: Boolean? = null
    var signatureCount: Long? = null
    var attestationObject: ByteArray? = null
    var clientDataJSON: ByteArray? = null
    var assertionRequest: AssertionRequest? = null

    fun getUserHandle(): ByteArray {
        return ByteArray.fromBase64(userIdBase64)
    }

    fun authenticationHasNotStarted(): Boolean {
        return assertionRequest == null
    }
}
