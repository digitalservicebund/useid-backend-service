package de.bund.digitalservice.useid.timebasedtokens

import de.bund.digitalservice.useid.identification.IdentificationSessionRepository
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.util.UUID

class InvalidUseIdSessionIdException : Exception("No session for useIdSessionId found")

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TimeBasedTokenService(private val mockDatasource: TBTMockDatasource, private val identificationSessionRepository: IdentificationSessionRepository) {
    private val log = KotlinLogging.logger {}

    fun updateOrCreate(useIdSessionId: UUID): TimeBasedToken {
        if (!identificationSessionRepository.existsByUseIdSessionId(useIdSessionId)) {
            throw InvalidUseIdSessionIdException()
        }
        try {
            mockDatasource.deleteAllByUseIdSessionId(useIdSessionId)
        } catch (e: Exception) {
            log.error("Error when deleting time based token", e)
        }
        return mockDatasource.save(TimeBasedToken(useIdSessionId, UUID.randomUUID()))
    }

    fun isTokenValid(useIdSessionId: UUID, tokenId: UUID): Boolean {
        val token = mockDatasource.findByUseIdSessionIdAndTokenId(useIdSessionId, tokenId)
        return token?.createdAt?.let { it.plusSeconds(60) > LocalDateTime.now() } ?: false
    }
}
