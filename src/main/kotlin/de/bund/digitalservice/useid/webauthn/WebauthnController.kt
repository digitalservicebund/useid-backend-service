package de.bund.digitalservice.useid.webauthn

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
import java.security.SecureRandom
import java.util.UUID

abstract class WebauthnController {

    private val rp: RelyingParty? = null

    private val random = SecureRandom()

    @PostMapping(path = ["/test/test"])
    fun test() {
        println("TEST!")
    }

    @PostMapping(path = ["/{wSId}/register"])
    fun startRegistration(@PathVariable wSId: UUID): ResponseEntity<PublicKeyCredentialCreationOptions> {
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

        val options = rp!!.startRegistration(startOptions)

        // WE NEED TO STORE THE REQUESTS SOMEWHERE IN MEMORY
        // val request = {
        //     test_username,
        //     test_credentialNickname,
        //     test_credentialNickname,
        //     generateRandom(32),
        //     rp!!.startRegistration(startOptions),
        // }

        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(options)
    }

    private fun generateRandom(length: Int = 32): ByteArray {
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return ByteArray(bytes)
    }
}
