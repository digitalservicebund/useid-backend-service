package de.bund.digitalservice.useid.identification

import java.util.UUID

data class IdentificationSession(
    val refreshAddress: String,
    val requestAttributes: List<String>,
    val sessionId: UUID,
    val tcTokenUrl: String
)
