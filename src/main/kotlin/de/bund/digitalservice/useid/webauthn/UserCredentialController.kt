package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartAssertionOptions
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import com.yubico.webauthn.exception.AssertionFailedException
import de.bund.digitalservice.useid.events.EventService
import de.bund.digitalservice.useid.events.EventType
import de.bund.digitalservice.useid.events.WidgetNotFoundException
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom
import java.util.UUID

internal const val CREDENTIALS_BASE_PATH = "/api/v1/credentials"

// TODO move logic from controller to service
@RestController
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class UserCredentialController(
    private val relyingParty: RelyingParty,
    private val userCredentialService: UserCredentialService,
    private val eventService: EventService,
) {
    private val log = KotlinLogging.logger {}

    @PostMapping(path = [CREDENTIALS_BASE_PATH])
    fun startRegistration(
        @RequestBody startRegistrationRequest: StartRegistrationRequest,
    ): ResponseEntity<Any> {
        val username = startRegistrationRequest.widgetSessionId.toString()
        val userId = generateId()

        val user = UserIdentity.builder()
            .name(username)
            .displayName(username)
            .id(userId)
            .build()

        val residentKeyRequirement = ResidentKeyRequirement.DISCOURAGED // OR: ResidentKeyRequirement.REQUIRED

        val startOptions = StartRegistrationOptions.builder()
            .user(user)
            .authenticatorSelection(
                AuthenticatorSelectionCriteria.builder()
                    .residentKey(residentKeyRequirement)
                    .build(),
            )
            .build()

        val pkcCreationOptions = relyingParty.startRegistration(startOptions)

        val userCredential = userCredentialService.create(
            startRegistrationRequest.widgetSessionId,
            username,
            userId.base64,
            startRegistrationRequest.refreshAddress,
            pkcCreationOptions,
        )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                StartRegistrationResponse(
                    userCredential.credentialId,
                    userCredential.pckCreationOptions.toCredentialsCreateJson(),
                ),
            )
    }

    @PutMapping(
        path = ["$CREDENTIALS_BASE_PATH/{credentialId}"],
        headers = ["Content-Type=application/json"],
    )
    fun completeRegistration(
        @PathVariable credentialId: UUID,
        @RequestBody publicKeyCredentialJson: String,
    ): ResponseEntity<Any> {
        val userCredential = userCredentialService.findByCredentialId(credentialId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val pkc = PublicKeyCredential.parseRegistrationResponseJson(publicKeyCredentialJson)

        val finishRegistrationOptions = FinishRegistrationOptions.builder()
            .request(userCredential.pckCreationOptions)
            .response(pkc)
            .build()

        val result = relyingParty.finishRegistration(finishRegistrationOptions)
        userCredentialService.updateWithRegistrationResult(credentialId, result, pkc)

        val assertionRequest: AssertionRequest = relyingParty.startAssertion(
            StartAssertionOptions.builder().username(userCredential.username).build(),
        )
        userCredentialService.updateWithAssertionRequest(credentialId, assertionRequest)

        val credentialGetJson = assertionRequest.toCredentialsGetJson()
        publishEvent(credentialGetJson, EventType.AUTHENTICATE, userCredential.widgetSessionId)

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .build()
    }

    @PostMapping(
        path = ["$CREDENTIALS_BASE_PATH/{credentialId}/authentications"], // TODO think about endpoint design
        headers = ["Content-Type=application/json"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun completeAuthentication(
        @PathVariable credentialId: UUID,
        @RequestBody publicKeyCredentialJson: String,
    ): ResponseEntity<Any> {
        val userCredential = userCredentialService.findByCredentialId(credentialId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()

        val pkc = PublicKeyCredential.parseAssertionResponseJson(publicKeyCredentialJson)

        val finishAssertionOptions = FinishAssertionOptions.builder()
            .request(userCredential.assertionRequest)
            .response(pkc)
            .build()

        try {
            val assertionResult = relyingParty.finishAssertion(finishAssertionOptions)
            if (assertionResult.isSuccess) {
                userCredentialService.delete(userCredential.credentialId)
                return ResponseEntity
                    .status(HttpStatus.OK)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(CompleteAuthenticationResponse(userCredential.refreshAddress))
            }
        } catch (e: AssertionFailedException) {
            log.error("Failed to finish WebAuthn authentication: {}", e.message, e)
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
    }

    // TODO resolve duplicate code with EventController
    /**
     * Receive events from the eID client (i.e. Ident-App) and publish them to the respective consumer.
     */
    fun publishEvent(data: Any, type: EventType, widgetSessionId: UUID): ResponseEntity<Nothing> {
        try {
            eventService.publish(data, type, widgetSessionId)
        } catch (e: WidgetNotFoundException) {
            log.info("Failed to publish event: ${e.message}")
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
        return ResponseEntity.status(HttpStatus.ACCEPTED).build()
    }

    private fun generateId(): ByteArray {
        val bytes = ByteArray(32)
        SecureRandom().nextBytes(bytes)
        return ByteArray(bytes)
    }
}
