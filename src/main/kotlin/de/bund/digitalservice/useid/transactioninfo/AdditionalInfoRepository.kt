package de.bund.digitalservice.useid.transactioninfo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Flux
import java.util.UUID

@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
interface AdditionalInfoRepository : ReactiveCrudRepository<AdditionalInfoDto, UUID> {
    fun findAllByUseidSessionId(useidSessionId: UUID): Flux<AdditionalInfoDto>
}
