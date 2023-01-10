package de.bund.digitalservice.useid.timebasedtokens

import java.time.LocalDateTime
import java.util.UUID

data class TimeBasedToken(
    val useIdSessionId: UUID,
    val tokenId: UUID,
    var createdAt: LocalDateTime? = null
)
