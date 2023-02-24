package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.AssertionRequest
import com.yubico.webauthn.FinishAssertionOptions
import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorAssertionResponse
import com.yubico.webauthn.data.AuthenticatorAttestationResponse
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.ClientAssertionExtensionOutputs
import com.yubico.webauthn.data.ClientRegistrationExtensionOutputs
import com.yubico.webauthn.data.PublicKeyCredential
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.PublicKeyCredentialRequestOptions
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

internal const val WEBAUTHN_BASE_PATH = "/api/v1/webauthn"

@RestController
class WebauthnController(private val relyingParty: RelyingParty) {

    private lateinit var publicKeyCredentialCreationOptions: PublicKeyCredentialCreationOptions

    @PostMapping(path = ["$WEBAUTHN_BASE_PATH/test"])
    fun test(): ResponseEntity<String> {
        println("TEST!")
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("OK")
    }

    @PostMapping(path = ["$WEBAUTHN_BASE_PATH/users"])
    fun startRegistration(): ResponseEntity<Any> {
        val testUsername = "TEST_USERNAME"
        val testDisplayname = "TEST_DISPLAYNAME"

        val userId = UserIdentity.builder()
            .name(testUsername)
            .displayName(testDisplayname)
            .id(generateRandom(32))
            .build()

        val residentKeyRequirement = ResidentKeyRequirement.DISCOURAGED // OR: ResidentKeyRequirement.REQUIRED

        val startOptions = StartRegistrationOptions.builder()
            .user(userId)
            .authenticatorSelection(
                AuthenticatorSelectionCriteria.builder()
                    .residentKey(residentKeyRequirement)
                    .build()
            )
            .build()

        publicKeyCredentialCreationOptions = relyingParty.startRegistration(startOptions)

        val resp = object {
            val userId = userId.id
            val challenge = publicKeyCredentialCreationOptions.challenge.base64
        }

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(resp)
    }

    @PostMapping(
        path = ["$WEBAUTHN_BASE_PATH/users/{userId}/{widgetSessionId}/complete"],
        headers = ["Content-Type=application/json"]
    )
    fun completeRegistration(
        @PathVariable userId: String,
        @PathVariable widgetSessionId: String,
        @RequestBody registrationCompleteResponse: RegistrationCompleteResponse
    ): ResponseEntity<Any> {
        val authenticatorAttestationResponse = AuthenticatorAttestationResponse
            .builder()
            .attestationObject(registrationCompleteResponse.attestationObject)
            .clientDataJSON(registrationCompleteResponse.clientDataJSON)
            .build()

        val clientRegistrationExtensionOutputs = ClientRegistrationExtensionOutputs
            .builder()
            .build()

        // val credential: PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>? = null

        val credentials = PublicKeyCredential
            .builder<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs>()
            .id(registrationCompleteResponse.rawId)
            .response(authenticatorAttestationResponse)
            .clientExtensionResults(clientRegistrationExtensionOutputs)
            .build()

        val finishRegistrationOptions = FinishRegistrationOptions.builder()
            .request(publicKeyCredentialCreationOptions) // cached from "registration start"
            .response(credentials)
            .build()

        val registrationResult = relyingParty.finishRegistration(finishRegistrationOptions)

        val resp = object {
            val userId = userId
            val widgetSessionId = widgetSessionId
            val registrationResponse_rawId = registrationCompleteResponse.rawId
            val registrationResponse_attestationObject = registrationCompleteResponse.attestationObject
            val registration_result = registrationResult
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(resp)
    }

    @PostMapping(
        path = ["$WEBAUTHN_BASE_PATH/users/{userId}/{widgetSessionId}/auth/complete"],
        headers = ["Content-Type=application/json"]
    )
    fun completeAuthentication(
        @PathVariable userId: String,
        @PathVariable widgetSessionId: String,
        @RequestBody authenticationCompleteResponse: AuthenticationCompleteResponse
    ): ResponseEntity<Any> {
        val publicKeyCredentialRequestOptions = PublicKeyCredentialRequestOptions
            .builder()
            .challenge(authenticationCompleteResponse.clientDataJSON) // TODO: THIS WILL BE CREATED IN THE START AUTH ROUTE (WHICH DOES NOT EXIST YET AND PROBABLY WILL BE HANDLED IN SSE)
            .build()

        val assertionRequest = AssertionRequest
            .builder()
            .publicKeyCredentialRequestOptions(publicKeyCredentialRequestOptions)
            .build()

        val authenticatorAssertionResponse = AuthenticatorAssertionResponse
            .builder()
            .authenticatorData(authenticationCompleteResponse.authenticatorData)
            .clientDataJSON(authenticationCompleteResponse.clientDataJSON)
            .signature(authenticationCompleteResponse.signature)
            .build()

        val clientAssertionExtensionOutputs = ClientAssertionExtensionOutputs
            .builder()
            .build()

        val response = PublicKeyCredential
            .builder<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs>()
            .id(authenticationCompleteResponse.clientDataJSON) // TODO: THIS NEEDS TO BE THE ID FROM REQ-BODY PROBABLY
            .response(authenticatorAssertionResponse)
            .clientExtensionResults(clientAssertionExtensionOutputs)
            .build()

        val finishAssertionOptions = FinishAssertionOptions
            .builder()
            .request(assertionRequest)
            .response(response)
            .build()

        val authResult = relyingParty.finishAssertion(finishAssertionOptions)

        val resp = object {
            val userId = userId
            val widgetSessionId = widgetSessionId
            val result = authResult
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(resp)
    }

    private fun generateRandom(length: Int = 32): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return ByteArray(bytes)
    }
}
