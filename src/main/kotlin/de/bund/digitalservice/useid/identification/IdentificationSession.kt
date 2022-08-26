package de.bund.digitalservice.useid.identification

import java.util.UUID

data class IdentificationSession(
    var eIDSessionId: UUID? = null,
    val refreshAddress: String,
    val requestDataGroups: List<String>,
    val useIDSessionId: UUID
)
