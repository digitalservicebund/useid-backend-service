package de.bund.digitalservice.useid.webauthn

import java.util.UUID

data class StartRegistrationResponse(
    val credentialId: UUID,
    val pkcCreationOptions: String,
)
