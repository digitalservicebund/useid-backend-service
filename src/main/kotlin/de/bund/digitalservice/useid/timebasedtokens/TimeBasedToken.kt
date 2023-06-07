// PROTOTYPE FILE

package de.bund.digitalservice.useid.timebasedtokens

import java.time.LocalDateTime
import java.util.UUID

data class TimeBasedToken(
    val sessionId: UUID,
    val tokenId: UUID,
    var createdAt: LocalDateTime? = null,
)
