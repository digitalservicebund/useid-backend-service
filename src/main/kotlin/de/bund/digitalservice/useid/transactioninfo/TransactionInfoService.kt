package de.bund.digitalservice.useid.transactioninfo

import mu.KotlinLogging
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.UUID

@Service
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TransactionInfoService(
    private val transactionInfoRepository: TransactionInfoRepository,
    private val additionalInfoRepository: AdditionalInfoRepository
) {

    private val log = KotlinLogging.logger {}

    fun create(useidSessionId: UUID, transactionInfo: TransactionInfo): Mono<TransactionInfoDto> {
        return transactionInfoRepository.save(TransactionInfoDto(useidSessionId, transactionInfo.providerName, transactionInfo.providerURL))
            .flatMap { transactionInfoDto ->
                createAdditionalInfo(transactionInfo.additionalInfo, useidSessionId)
                    .collectList()
                    .doOnNext { additionalInfoDto -> transactionInfoDto.additionalInfo = additionalInfoDto }
                    .then(Mono.just(transactionInfoDto))
            }
            .doOnNext {
                log.info("Created new transaction info. useidSessionId=${it.useidSessionId}, transactionInfoId=${it.id}")
            }.doOnError {
                log.error("Failed to create transaction info: ${it.message}", it)
            }
    }

    private fun createAdditionalInfo(additionalInformation: List<AdditionalInfo>, useidSessionId: UUID): Flux<AdditionalInfoDto> {
        val additionalInfos = additionalInformation.map {
            AdditionalInfoDto(useidSessionId, it.key, it.value)
        }.toList()
        return additionalInfoRepository.saveAll(additionalInfos).doOnNext {
            log.info("Created additional infos for transaction info. useidSessionId=$useidSessionId")
        }.doOnError {
            log.error("Failed to create additional info for transaction info: ${it.message}. useidSessionId=$useidSessionId", it)
        }
    }

    fun findByUseIDSessionId(useidSessionId: UUID): Mono<TransactionInfoDto> {
        return transactionInfoRepository.findByUseidSessionId(useidSessionId)
            .flatMap { transactionInfoDto ->
                additionalInfoRepository.findAllByUseidSessionId(useidSessionId)
                    .collectList()
                    .doOnNext { additionalInfo -> transactionInfoDto.additionalInfo = additionalInfo }
                    .then(Mono.just(transactionInfoDto))
            }
            .doOnNext {
                log.info("Created new transaction info. useidSessionId=${it.useidSessionId}")
            }.doOnError {
                log.error("Failed to create transaction info: ${it.message}. useidSessionId=$useidSessionId", it)
            }
    }
}
