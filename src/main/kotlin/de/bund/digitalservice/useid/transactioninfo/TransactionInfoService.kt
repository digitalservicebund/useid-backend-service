package de.bund.digitalservice.useid.transactioninfo

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoService(
    private val transactionInfoMockDatasource: TransactionInfoMockDatasource
) {

    private val log = KotlinLogging.logger {}

    fun create(useIdSessionId: UUID, transactionInfo: TransactionInfo): Mono<TransactionInfoDto> {
        return transactionInfoMockDatasource.deleteAllByUseIdSessionId(useIdSessionId)
            .then(transactionInfoMockDatasource.save(TransactionInfoDto(useIdSessionId, transactionInfo.providerName, transactionInfo.providerURL, transactionInfo.additionalInfo)))
            .doOnNext {
                log.info("Created new transaction info. useIdSessionId=${it.useIdSessionId}")
            }.doOnError {
                log.error("Failed to create transaction info: ${it.message}", it)
            }
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<TransactionInfoDto> {
        return transactionInfoMockDatasource.findByUseIdSessionId(useIdSessionId)
            .doOnError {
                log.error("Could not find transaction info: ${it.message}. useIdSessionId=$useIdSessionId", it)
            }
    }
}
