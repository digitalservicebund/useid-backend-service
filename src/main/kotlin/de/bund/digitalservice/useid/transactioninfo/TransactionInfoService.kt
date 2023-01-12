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

    fun create(useIdSessionId: UUID, transactionInfo: TransactionInfo): Mono<TransactionInfoDto> {
        return transactionInfoRepository.save(TransactionInfoDto(useIdSessionId, transactionInfo.providerName, transactionInfo.providerURL))
            .flatMap { transactionInfoDto ->
                createAdditionalInfo(transactionInfo.additionalInfo, useIdSessionId)
                    .collectList()
                    .doOnNext { additionalInfoDto -> transactionInfoDto.additionalInfo = additionalInfoDto }
                    .then(Mono.just(transactionInfoDto))
            }
            .doOnNext {
                log.info("Created new transaction info. useIdSessionId=${it.useIdSessionId}, transactionInfoId=${it.id}")
            }.doOnError {
                log.error("Failed to create transaction info: ${it.message}", it)
            }
    }

    private fun createAdditionalInfo(additionalInformation: List<AdditionalInfo>, useIdSessionId: UUID): Flux<AdditionalInfoDto> {
        val additionalInfos = additionalInformation.map {
            AdditionalInfoDto(useIdSessionId, it.key, it.value)
        }.toList()
        return additionalInfoRepository.saveAll(additionalInfos).doOnNext {
            log.info("Created additional infos for transaction info. useIdSessionId=$useIdSessionId")
        }.doOnError {
            log.error("Failed to create additional info for transaction info: ${it.message}. useIdSessionId=$useIdSessionId", it)
        }
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<TransactionInfoDto> {
        return transactionInfoRepository.findByUseIdSessionId(useIdSessionId)
            .flatMap { transactionInfoDto ->
                additionalInfoRepository.findAllByUseIdSessionId(useIdSessionId)
                    .collectList()
                    .doOnNext { additionalInfo -> transactionInfoDto.additionalInfo = additionalInfo }
                    .then(Mono.just(transactionInfoDto))
            }
            .doOnError {
                log.error("Could not find transaction info: ${it.message}. useIdSessionId=$useIdSessionId", it)
            }
    }
}
