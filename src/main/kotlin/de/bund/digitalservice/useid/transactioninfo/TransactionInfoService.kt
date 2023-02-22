package de.bund.digitalservice.useid.transactioninfo

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoService(
    private val transactionInfoMockDatasource: TransactionInfoMockDatasource,
) {

    private val log = KotlinLogging.logger {}

    fun create(useIdSessionId: UUID, transactionInfo: TransactionInfo): TransactionInfoDto {
        try {
            transactionInfoMockDatasource.deleteAllByUseIdSessionId(useIdSessionId)
        } catch (e: Exception) {
            log.error("Failed to delete transaction info: ${e.message}", e)
        }

        log.info("Created new transaction info. useIdSessionId=$useIdSessionId")
        return transactionInfoMockDatasource.save(TransactionInfoDto(useIdSessionId, transactionInfo.providerName, transactionInfo.providerURL, transactionInfo.additionalInfo))
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): TransactionInfoDto? {
        return transactionInfoMockDatasource.findByUseIdSessionId(useIdSessionId)
    }
}
