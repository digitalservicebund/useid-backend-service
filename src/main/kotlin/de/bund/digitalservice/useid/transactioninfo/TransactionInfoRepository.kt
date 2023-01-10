package de.bund.digitalservice.useid.transactioninfo

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface TransactionInfoRepository : ReactiveCrudRepository<TransactionInfo, UUID> {
    fun findByUseidSessionId(useIDSessionId: UUID): Mono<TransactionInfo>
}
