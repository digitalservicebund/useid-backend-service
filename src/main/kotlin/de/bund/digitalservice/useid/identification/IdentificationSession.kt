package de.bund.digitalservice.useid.identification

import java.util.UUID

data class IdentificationSession(
    var eIDSessionId: UUID = UUID(0, 0),
    val refreshAddress: String,
    val requestAttributes: List<String>,
    val useIDSessionId: UUID
)
