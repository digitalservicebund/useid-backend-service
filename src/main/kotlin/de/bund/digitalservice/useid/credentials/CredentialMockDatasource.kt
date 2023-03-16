package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.data.ByteArray
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class CredentialMockDatasource {
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
}
