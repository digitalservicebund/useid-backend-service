package de.bund.digitalservice.useid.transactioninfo

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Repository
import java.util.UUID

@Repository
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoMockDatasource {
    private val transactionInfoList = mutableListOf<TransactionInfoDto>()

    fun save(transactionInfo: TransactionInfoDto): TransactionInfoDto {
        transactionInfoList.add(transactionInfo)
        return transactionInfo
    }

    fun deleteAllByUseIdSessionId(useIdSessionId: UUID) {
        transactionInfoList.removeAll { it.useIdSessionId == useIdSessionId }
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): TransactionInfoDto? {
        return transactionInfoList.find { it.useIdSessionId == useIdSessionId }
    }
}
