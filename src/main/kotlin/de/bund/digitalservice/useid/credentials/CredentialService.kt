package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.RegistrationResult
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import javassist.NotFoundException
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class CredentialService(
    private val credentialMockDatasource: CredentialMockDatasource,
) : CredentialRepository {

    private val log = KotlinLogging.logger {}

    fun create(
        widgetSessionId: UUID,
        username: String,
        userIdBase64: String,
        refreshAddress: String,
        pckCreationOptions: PublicKeyCredentialCreationOptions,
    ): Credential {
        val credentialId = UUID.randomUUID()
        val credential = credentialMockDatasource.save(
            Credential(credentialId, widgetSessionId, username, userIdBase64, refreshAddress, pckCreationOptions),
        )
        log.info("Created new user credential. credentialId=$credentialId")
        return credential
    }

    fun updateWithRegistrationResult(
        credentialId: UUID,
        registrationResult: RegistrationResult,
        pkc: PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>,
    ) {
        val userCredential = credentialMockDatasource.findById(credentialId)
            ?: throw NotFoundException("Could not find user credential. credentialId=$credentialId")

        userCredential.keyId = registrationResult.keyId
        userCredential.publicKeyCose = registrationResult.publicKeyCose
        userCredential.isDiscoverable = registrationResult.isDiscoverable.getOrDefault(false)
        userCredential.signatureCount = registrationResult.signatureCount
        userCredential.attestationObject = pkc.response.attestationObject
        userCredential.clientDataJSON = pkc.response.clientDataJSON

        credentialMockDatasource.update(userCredential)
    }

    fun updateWithAssertionRequest(
        credentialId: UUID,
        assertionRequest: AssertionRequest,
    ) {
        val userCredential = credentialMockDatasource.findById(credentialId)
            ?: throw NotFoundException("Could not find user credential. credentialId=$credentialId")
        userCredential.assertionRequest = assertionRequest
        credentialMockDatasource.update(userCredential)
    }

    fun findByCredentialId(credentialId: UUID): Credential? {
        return credentialMockDatasource.findById(credentialId)
    }

    fun delete(credentialId: UUID) {
        credentialMockDatasource.delete(credentialId)
    }

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        val userCredential = credentialMockDatasource.findByUsername(username) ?: return mutableSetOf()
        return mutableSetOf(userCredential.keyId!!)
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        val userCredential = credentialMockDatasource.findByUsername(username) ?: return Optional.empty()
        return Optional.of(userCredential.getUserHandle())
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray): Optional<String> {
        val userCredential = credentialMockDatasource.findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(userCredential.username)
    }

    override fun lookup(credentialId: ByteArray, userHandle: ByteArray): Optional<RegisteredCredential> {
        val userCredential = credentialMockDatasource.findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(createRegisteredCredential(userCredential))
    }

    override fun lookupAll(credentialId: ByteArray): MutableSet<RegisteredCredential> {
        val userCredential = credentialMockDatasource.findByKeyCredentialId(credentialId) ?: return mutableSetOf()
        return mutableSetOf(createRegisteredCredential(userCredential))
    }

    private fun createRegisteredCredential(credential: Credential): RegisteredCredential =
        RegisteredCredential.builder()
            .credentialId(credential.keyId!!.id)
            .userHandle(credential.getUserHandle())
            .publicKeyCose(credential.publicKeyCose)
            .build()
}
