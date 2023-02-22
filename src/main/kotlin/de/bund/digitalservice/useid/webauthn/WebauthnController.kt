package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.FinishRegistrationOptions
import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

internal const val WEBAUTHN_BASE_PATH = "/webauthn"

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
            val challenge = publicKeyCredentialCreationOptions.challenge
        }

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(resp)
    }

    @PostMapping(path = ["$WEBAUTHN_BASE_PATH/users/{userId}/{widgetSessionId}/credentials"])
    fun finishRegistration(@PathVariable userId: String, @PathVariable widgetSessionId: String): ResponseEntity<Any> {
        val responseJson = ""
        val response = jsonMapper.readValue("INPUT", RegistrationResponse::class.java)

        val finishRegistrationOptions = FinishRegistrationOptions.builder()
            .request(publicKeyCredentialCreationOptions) // cached from "registration start"
            .response(response.getCredential())
            .build()
        relyingParty.finishRegistration()

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("$userId - $widgetSessionId")
    }

    private fun generateRandom(length: Int = 32): ByteArray {
        val bytes = ByteArray(length)
        SecureRandom().nextBytes(bytes)
        return ByteArray(bytes)
    }
}
