package de.bund.digitalservice.useid.timebasedtokens

import de.bund.digitalservice.useid.identification.IdentificationSessionService
import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

class InvalidUseIdSessionIdException : Exception("No session for useIdSessionId found")

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TimeBasedTokenService(private val mockDatasource: TBTMockDatasource, private val identificationSessionService: IdentificationSessionService) {
    private val log = KotlinLogging.logger {}

    fun updateOrCreate(useIdSessionId: UUID): Mono<TimeBasedToken> {
        identificationSessionService.findByUseIdSessionId(useIdSessionId) ?: throw InvalidUseIdSessionIdException()
        try {
            mockDatasource.deleteAllByUseIdSessionId(useIdSessionId)
        } catch (e: Exception) {
            log.error("Error when deleting time based token", e)
        }
        return Mono.just(mockDatasource.save(TimeBasedToken(useIdSessionId, UUID.randomUUID())))
    }

    fun isTokenValid(useIdSessionId: UUID, tokenId: UUID): Mono<Boolean> {
        val token = mockDatasource.findByUseIdSessionIdAndTokenId(useIdSessionId, tokenId)
        return Mono.just(token?.createdAt?.let { it.plusSeconds(60) > LocalDateTime.now() } ?: false)
    }
}
