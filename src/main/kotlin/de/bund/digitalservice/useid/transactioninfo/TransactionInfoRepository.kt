package de.bund.digitalservice.useid.transactioninfo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
interface TransactionInfoRepository : ReactiveCrudRepository<TransactionInfo, UUID> {
    fun findByUseidSessionId(useIDSessionId: UUID): Mono<TransactionInfo>
}
