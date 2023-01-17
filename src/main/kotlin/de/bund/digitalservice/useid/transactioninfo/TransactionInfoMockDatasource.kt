package de.bund.digitalservice.useid.transactioninfo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoMockDatasource {
    private val transactionInfoList = mutableListOf<TransactionInfoDto>()

    fun save(transactionInfo: TransactionInfoDto): Mono<TransactionInfoDto> {
        transactionInfoList.add(transactionInfo)
        return Mono.just(transactionInfo)
    }

    fun deleteAllByUseIdSessionId(useIdSessionId: UUID): Mono<Void> {
        transactionInfoList.removeAll { it.useIdSessionId == useIdSessionId }
        return Mono.empty()
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<TransactionInfoDto> {
        return Mono.justOrEmpty(transactionInfoList.find { it.useIdSessionId == useIdSessionId })
    }
}
