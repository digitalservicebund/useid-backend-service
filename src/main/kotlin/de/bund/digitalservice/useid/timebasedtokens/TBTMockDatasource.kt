package de.bund.digitalservice.useid.timebasedtokens

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
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

    fun deleteAllByUseIdSessionId(useIdSessionId: UUID) {
        timeBasedTokens.removeAll { it.useIdSessionId == useIdSessionId }
    }

    fun findByUseIdSessionIdAndTokenId(useIdSessionId: UUID, tokenId: UUID): TimeBasedToken? {
        return timeBasedTokens.find { it.useIdSessionId == useIdSessionId && it.tokenId == tokenId }
    }
}
