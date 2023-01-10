package de.bund.digitalservice.useid.transactioninfo

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class TransactionInfoService(private val transactionInfoRepository: TransactionInfoRepository) {

    private val log = KotlinLogging.logger {}

    fun createOrUpdate(useIDSessionId: UUID, providerName: String, providerURL: String, additionalInformation: String): Mono<TransactionInfo> {
        return transactionInfoRepository.findByUseidSessionId(useIDSessionId)
            .defaultIfEmpty(TransactionInfo(useIDSessionId, providerName, providerURL, additionalInformation))
            .flatMap { transactionInfoRepository.save(it) }
            .doOnNext {
                log.info("Created new identification session. useIDSessionId=${it.useidSessionId}")
            }.doOnError {
                log.error("Failed to create identification session: ${it.message}")
            }
    }

    fun findByUseIDSessionId(useIDSessionId: UUID): Mono<TransactionInfo> {
        return transactionInfoRepository.findByUseidSessionId(useIDSessionId)
    }
}
