// PROTOTYPE FILE

package de.bund.digitalservice.useid.timebasedtokens

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TBTMockDatasource {
    private val timeBasedTokens = mutableListOf<TimeBasedToken>()

    fun save(timeBasedToken: TimeBasedToken): TimeBasedToken {
        timeBasedToken.createdAt = LocalDateTime.now()
        timeBasedTokens.add(timeBasedToken)
        return timeBasedToken
    }

    fun deleteAllBySessionId(sessionId: UUID) {
        timeBasedTokens.removeAll { it.sessionId == sessionId }
    }

    fun findBySessionIdAndTokenId(sessionId: UUID, tokenId: UUID): TimeBasedToken? {
        return timeBasedTokens.find { it.sessionId == sessionId && it.tokenId == tokenId }
    }
}
