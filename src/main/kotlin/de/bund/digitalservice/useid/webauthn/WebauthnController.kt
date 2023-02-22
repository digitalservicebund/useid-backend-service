package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.StartRegistrationOptions
import com.yubico.webauthn.data.AuthenticatorSelectionCriteria
import com.yubico.webauthn.data.ByteArray
import com.yubico.webauthn.data.ResidentKeyRequirement
import com.yubico.webauthn.data.UserIdentity
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.security.SecureRandom

@RestController
class WebauthnController(private val relyingParty: RelyingParty) {

    @PostMapping(path = ["/webauthn/test"])
    fun test(): ResponseEntity<String> {
        println("TEST!")
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("OK")
    }

    @PostMapping(path = ["/webauthn/users"])
    fun startRegistration(): ResponseEntity<Any> {
        val test_username = "TEST_USERNAME"
        val test_displayName = "TEST_DISPLAYNAME"
        val test_credentialNickname = "TEST_CREDENTIAL_NICKNAME"

        val userId = UserIdentity.builder()
            .name(test_username)
            .displayName(test_displayName)
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

        val publicKeyCredentialCreationOptions = relyingParty.startRegistration(startOptions)

        val resp = object {
            val userId = userId.id
            val challenge = publicKeyCredentialCreationOptions.challenge
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
