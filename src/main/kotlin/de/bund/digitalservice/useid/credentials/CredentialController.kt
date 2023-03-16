package de.bund.digitalservice.useid.credentials

import de.bund.digitalservice.useid.events.AuthenticateEvent
import de.bund.digitalservice.useid.events.EventService
import de.bund.digitalservice.useid.events.EventType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val CREDENTIALS_BASE_PATH = "/api/v1/credentials"

// TODO move logic from controller to service
@RestController
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class CredentialController(
    private val credentialService: CredentialService,
    private val eventService: EventService,
) {
    @PostMapping(path = [CREDENTIALS_BASE_PATH])
    fun startRegistration(
        @RequestBody startRegistrationRequest: StartRegistrationRequest,
    ): ResponseEntity<Any> {
        val credential = credentialService.startRegistration(
            startRegistrationRequest.widgetSessionId,
            startRegistrationRequest.refreshAddress,
        )

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                StartRegistrationResponse(
                    credential.credentialId,
                    credential.pckCreationOptions.toCredentialsCreateJson(),
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
        credentialService.finishRegistration(credentialId, publicKeyCredentialJson)

        val credential = credentialService.startAuthentication(credentialId)
        sendAuthenticateEventToWidget(credential)

        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .contentType(MediaType.APPLICATION_JSON)
            .build()
    }

    private fun sendAuthenticateEventToWidget(credential: Credential) {
        eventService.publish(
            AuthenticateEvent(credential.credentialId, credential.assertionRequest!!.toCredentialsGetJson()),
            EventType.AUTHENTICATE,
            credential.widgetSessionId,
        )
    }

    @PostMapping(
        path = ["$CREDENTIALS_BASE_PATH/{credentialId}/authentications"],
        headers = ["Content-Type=application/json"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    fun completeAuthentication(
        @PathVariable credentialId: UUID,
        @RequestBody publicKeyCredentialJson: String,
    ): ResponseEntity<Any> {
        val refreshAddress = credentialService.finishAuthentication(credentialId, publicKeyCredentialJson)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(CompleteAuthenticationResponse(refreshAddress))
    }
}
