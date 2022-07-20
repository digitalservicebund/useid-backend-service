package de.bund.digitalservice.useid.identification

import java.util.UUID

data class CreateIdentitySessionResponse(
    val tcTokenUrl: String,
    val sessionId: UUID
)
