// PROTOTYPE FILE

package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.Optional
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class CredentialMockDatasource : CredentialRepository {
    private val credentials = mutableListOf<Credential>()

    fun save(credential: Credential): Credential {
        credentials.add(credential)
        return credential
    }

    fun update(credential: Credential): Credential {
        credentials.removeAll { it.credentialId == credential.credentialId }
        credentials.add(credential)
        return credential
    }

    fun findById(credentialId: UUID): Credential? {
        return credentials.find { it.credentialId == credentialId }
    }

    fun findByUsername(username: String): Credential? {
        return credentials.find { it.username == username }
    }

    fun findByUserId(userIdBase64: String): Credential? {
        return credentials.find { it.userIdBase64 == userIdBase64 }
    }

    fun findByKeyCredentialId(keyCredentialId: ByteArray): Credential? {
        return credentials.find { it.keyId?.id == keyCredentialId }
    }

    fun delete(credentialId: UUID) {
        credentials.removeAll { it.credentialId == credentialId }
    }

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        val credential = findByUsername(username) ?: return mutableSetOf()
        val keyId = credential.keyId ?: return mutableSetOf()
        return mutableSetOf(keyId)
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        val credential = findByUsername(username) ?: return Optional.empty()
        return Optional.of(credential.getUserHandle())
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray): Optional<String> {
        val credential = findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(credential.username)
    }

    override fun lookup(credentialId: ByteArray, userHandle: ByteArray): Optional<RegisteredCredential> {
        val credential = findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(createRegisteredCredential(credential))
    }

    override fun lookupAll(credentialId: ByteArray): MutableSet<RegisteredCredential> {
        val credential = findByKeyCredentialId(credentialId) ?: return mutableSetOf()
        return mutableSetOf(createRegisteredCredential(credential))
    }

    private fun createRegisteredCredential(credential: Credential): RegisteredCredential =
        RegisteredCredential.builder()
            .credentialId(credential.keyId!!.id)
            .userHandle(credential.getUserHandle())
            .publicKeyCose(credential.publicKeyCose)
            .build()
}
