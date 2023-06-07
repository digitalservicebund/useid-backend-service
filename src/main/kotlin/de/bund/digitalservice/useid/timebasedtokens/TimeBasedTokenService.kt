package de.bund.digitalservice.useid.timebasedtokens

import de.bund.digitalservice.useid.identification.IdentificationSessionRepository
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

class InvalidSessionIdException : Exception("No session for this sessionId found")

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TimeBasedTokenService(private val mockDatasource: TBTMockDatasource, private val identificationSessionRepository: IdentificationSessionRepository) {
    private val log = KotlinLogging.logger {}

    fun updateOrCreate(sessionId: UUID): TimeBasedToken {
        if (!identificationSessionRepository.existsByUseIdSessionId(sessionId)) {
            throw InvalidSessionIdException()
        }
        try {
            mockDatasource.deleteAllBySessionId(sessionId)
        } catch (e: Exception) {
            log.error("Error when deleting time based token", e)
        }
        return mockDatasource.save(TimeBasedToken(sessionId, UUID.randomUUID()))
    }

    fun isTokenValid(sessionId: UUID, tokenId: UUID): Boolean {
        val token = mockDatasource.findBySessionIdAndTokenId(sessionId, tokenId)
        return token?.createdAt?.let { it.plusSeconds(60) > LocalDateTime.now() } ?: false
    }
}
