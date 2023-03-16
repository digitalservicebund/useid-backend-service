package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.CredentialRepository
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RegisteredCredential
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartAssertionOptions
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import com.yubico.webauthn.exception.AssertionFailedException
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.util.Optional
import java.util.UUID
import kotlin.jvm.optionals.getOrDefault

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class CredentialService(
    private val credentialMockDatasource: CredentialMockDatasource,
    private val relyingParty: RelyingParty,
) : CredentialRepository {

    private val log = KotlinLogging.logger {}

    fun startRegistration(widgetSessionId: UUID, refreshAddress: String): Credential {
        val user = buildUser(widgetSessionId)
        val startOptions = buildStartRegistrationOptions(user)

        val pkcCreationOptions = relyingParty.startRegistration(startOptions)

        val credential = credentialMockDatasource.save(
            Credential(
                UUID.randomUUID(),
                widgetSessionId,
                user.name,
                user.id.base64,
                refreshAddress,
                pkcCreationOptions,
            ),
        )
        log.info("Created new user credential. credentialId=${UUID.randomUUID()}")
        return credential
    }

    private fun buildUser(widgetSessionId: UUID): UserIdentity {
        val username = widgetSessionId.toString()
        return UserIdentity.builder()
            .name(username)
            .displayName(username)
            .id(generateUserId())
            .build()
    }

    private fun buildStartRegistrationOptions(user: UserIdentity): StartRegistrationOptions? {
        return StartRegistrationOptions.builder()
            .user(user)
            .authenticatorSelection(
                AuthenticatorSelectionCriteria.builder()
                    .residentKey(ResidentKeyRequirement.DISCOURAGED)
                    .build(),
            )
            .build()
    }

    fun finishRegistration(
        credentialId: UUID,
        publicKeyCredentialJson: String,
    ): Credential {
        val credential = credentialMockDatasource.findById(credentialId)
            ?: throw CredentialNotFoundException(credentialId)

        val pkc = PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson)

        val finishRegistrationOptions = FinishRegistrationOptions.builder()
            .request(credential.pckCreationOptions)
            .response(pkc)
            .build()

        val result = relyingParty.finishRegistration(finishRegistrationOptions)

        credential.keyId = result.keyId
        credential.publicKeyCose = result.publicKeyCose
        credential.isDiscoverable = result.isDiscoverable.getOrDefault(false)
        credential.signatureCount = result.signatureCount
        credential.attestationObject = pkc.response.attestationObject
        credential.clientDataJSON = pkc.response.clientDataJSON

        return credentialMockDatasource.update(credential)
    }

    fun startAuthentication(credentialId: UUID): Credential {
        val credential = credentialMockDatasource.findById(credentialId)
            ?: throw CredentialNotFoundException(credentialId)

        val assertionRequest: AssertionRequest = relyingParty.startAssertion(
            StartAssertionOptions.builder().username(credential.username).build(),
        )

        credential.assertionRequest = assertionRequest
        return credentialMockDatasource.update(credential)
    }

    fun finishAuthentication(credentialId: UUID, publicKeyCredentialJson: String): String {
        val credential = credentialMockDatasource.findById(credentialId)
            ?: throw CredentialNotFoundException(credentialId)

        val pkc = PublicKeyCredential.parseAssertionResponseJson(publicKeyCredentialJson)

        val finishAssertionOptions = FinishAssertionOptions.builder()
            .request(credential.assertionRequest)
            .response(pkc)
            .build()

        val assertionResult = relyingParty.finishAssertion(finishAssertionOptions)
        if (!assertionResult.isSuccess) {
            throw AssertionFailedException("WebAuthn assertion failed. credentialsId=$credentialId")
        }

        credentialMockDatasource.delete(credentialId)

        return credential.refreshAddress
    }

    override fun getCredentialIdsForUsername(username: String): MutableSet<PublicKeyCredentialDescriptor> {
        val credential = credentialMockDatasource.findByUsername(username) ?: return mutableSetOf()
        return mutableSetOf(credential.keyId!!)
    }

    override fun getUserHandleForUsername(username: String): Optional<ByteArray> {
        val credential = credentialMockDatasource.findByUsername(username) ?: return Optional.empty()
        return Optional.of(credential.getUserHandle())
    }

    override fun getUsernameForUserHandle(userHandle: ByteArray): Optional<String> {
        val credential = credentialMockDatasource.findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(credential.username)
    }

    override fun lookup(credentialId: ByteArray, userHandle: ByteArray): Optional<RegisteredCredential> {
        val credential = credentialMockDatasource.findByUserId(userHandle.base64) ?: return Optional.empty()
        return Optional.of(createRegisteredCredential(credential))
    }

    override fun lookupAll(credentialId: ByteArray): MutableSet<RegisteredCredential> {
        val credential = credentialMockDatasource.findByKeyCredentialId(credentialId) ?: return mutableSetOf()
        return mutableSetOf(createRegisteredCredential(credential))
    }

    private fun createRegisteredCredential(credential: Credential): RegisteredCredential =
        RegisteredCredential.builder()
            .credentialId(credential.keyId!!.id)
            .userHandle(credential.getUserHandle())
            .publicKeyCose(credential.publicKeyCose)
            .build()

    private fun generateUserId(): ByteArray {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return ByteArray(bytes)
    }
}
