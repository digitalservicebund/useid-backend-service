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
        return identificationSessionService.findByUseIdSessionId(useIdSessionId)
            .switchIfEmpty(Mono.error(InvalidUseIdSessionIdException()))
            .flatMap {
                mockDatasource.deleteAllByUseIdSessionId(useIdSessionId)
                    .onErrorResume {
                        log.error("Error when deleting time based token", it)
                        Mono.empty<Void>()
                    }
                    .then(mockDatasource.save(TimeBasedToken(useIdSessionId, UUID.randomUUID())))
            }
    }

    fun isTokenValid(useIdSessionId: UUID, tokenId: UUID): Mono<Boolean> {
        return mockDatasource.findByUseIdSessionIdAndTokenId(useIdSessionId, tokenId)
            .mapNotNull { it?.let { it.createdAt != null && it.createdAt!!.plusSeconds(60) > LocalDateTime.now() } }
            .defaultIfEmpty(false)
    }
}
