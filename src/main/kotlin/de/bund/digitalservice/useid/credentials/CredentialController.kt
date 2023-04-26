package de.bund.digitalservice.useid.credentials

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eventstreams.AuthenticateEvent
import de.bund.digitalservice.useid.eventstreams.EventStreamService
import de.bund.digitalservice.useid.eventstreams.EventType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val CREDENTIALS_BASE_PATH = "${ApplicationProperties.apiVersionPrefix}/credentials"

@RestController
@RequestMapping(CREDENTIALS_BASE_PATH)
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
@Tag(
    name = "Credentials",
    description = "WebAuthn credentials are used to authenticate the a user in the widget.",
)
class CredentialController(
    private val credentialService: CredentialService,
    private val eventStreamService: EventStreamService,
) {
    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Start registration for WebAuthn credentials.")
    @ApiResponse(responseCode = "201", content = [Content()])
    fun startRegistration(
        @RequestBody startRegistrationRequest: StartRegistrationRequest,
    ): ResponseEntity<Any> {
        val credential = credentialService.startRegistration(
            startRegistrationRequest.widgetSessionId,
            startRegistrationRequest.refreshAddress,
        )

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                StartRegistrationResponse(
                    credential.credentialId,
                    credential.pkcCreationOptions.toCredentialsCreateJson(),
                ),
            )
    }

    @PutMapping(
        path = ["/{credentialId}"],
    )
    @Operation(summary = "Complete registration for WebAuthn credentials and start authentication. Notifies the widget about the started authentication.")
    @ApiResponse(responseCode = "204", content = [Content()])
    fun completeRegistration(
        @PathVariable credentialId: UUID,
        @RequestBody publicKeyCredentialJson: String,
    ): ResponseEntity<Void> {
        credentialService.finishRegistration(credentialId, publicKeyCredentialJson)

        val credential = credentialService.startAuthentication(credentialId)
        sendAuthenticateEventToWidget(credential)

        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build()
    }

    private fun sendAuthenticateEventToWidget(credential: Credential) {
        if (credential.authenticationHasNotStarted()) {
            throw IllegalStateException("Authentication with credentials has not been started yet. credentialId=${credential.credentialId}")
        }

        eventStreamService.publish(
            AuthenticateEvent(credential.credentialId, credential.assertionRequest!!.toCredentialsGetJson()),
            EventType.AUTHENTICATE,
            credential.widgetSessionId,
        )
    }

    @PostMapping(
        path = ["/{credentialId}/authentications"],
        produces = [MediaType.APPLICATION_JSON_VALUE],
    )
    @Operation(summary = "Complete authentication with WebAuthn credentials.")
    @ApiResponse(responseCode = "200", content = [Content()])
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
