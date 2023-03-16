package de.bund.digitalservice.useid.credentials

import java.util.UUID

data class StartRegistrationResponse(
    val credentialId: UUID,
    val pkcCreationOptions: String,
)
