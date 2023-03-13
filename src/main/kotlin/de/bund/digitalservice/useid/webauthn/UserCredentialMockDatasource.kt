package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.data.ByteArray
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class UserCredentialMockDatasource {
    private val userCredentials = mutableListOf<UserCredential>()

    fun save(userCredential: UserCredential): UserCredential {
        userCredentials.add(userCredential)
        return userCredential
    }

    fun update(userCredential: UserCredential) {
        userCredentials.removeAll { it.credentialId == userCredential.credentialId }
        userCredentials.add(userCredential)
    }

    fun findById(credentialId: UUID): UserCredential? {
        return userCredentials.find { it.credentialId == credentialId }
    }

    fun findByUsername(username: String): UserCredential? {
        return userCredentials.find { it.username == username }
    }

    fun findByUserId(userIdBase64: String): UserCredential? {
        return userCredentials.find { it.userIdBase64 == userIdBase64 }
    }

    fun findByKeyCredentialId(keyCredentialId: ByteArray): UserCredential? {
        return userCredentials.find { it.keyId?.id == keyCredentialId }
    }

    fun delete(credentialId: UUID) {
        userCredentials.removeAll { it.credentialId == credentialId }
    }
}
