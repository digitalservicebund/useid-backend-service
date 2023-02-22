package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import java.util.Optional

class UserCredentialRepository : CredentialRepository {
    override fun getCredentialIdsForUsername(username: String?): MutableSet<PublicKeyCredentialDescriptor> {
        return mutableSetOf<PublicKeyCredentialDescriptor>()
    }

    override fun getUserHandleForUsername(username: String?): Optional<ByteArray> {
        return Optional.empty()
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray?): Optional<String> {
        return Optional.empty()
    }

    override fun lookup(credentialId: ByteArray?, userHandle: ByteArray?): Optional<RegisteredCredential> {
        return Optional.empty()
    }

    override fun lookupAll(credentialId: ByteArray?): MutableSet<RegisteredCredential> {
        return mutableSetOf<RegisteredCredential>()
    }
}
