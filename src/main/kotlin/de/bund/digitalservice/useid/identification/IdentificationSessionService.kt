package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val identificationSessionRepository: IdentificationSessionRepository) {

    private val log = KotlinLogging.logger {}

    fun create(refreshAddress: String, requestDataGroups: List<String>): Mono<IdentificationSession> {
        return identificationSessionRepository.save(IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups))
            .doOnNext {
                log.info("Created new identification session. useIdSessionId=${it.useIdSessionId}")
            }.doOnError {
                log.error("Failed to create identification session: ${it.message}")
            }
    }

    fun findByEIDSessionId(eIdSessionId: UUID): Mono<IdentificationSession> {
        return identificationSessionRepository.findByEIdSessionId(eIdSessionId)
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<IdentificationSession> {
        return identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
    }

    fun updateEIDSessionId(useIdSessionId: UUID, eIdSessionId: UUID): Mono<IdentificationSession> {
        return findByUseIdSessionId(useIdSessionId).flatMap {
            it.eIdSessionId = eIdSessionId
            identificationSessionRepository.save(it)
        }.doOnNext {
            log.info("Updated eIdSessionId of identification session. useIdSessionId=${it.useIdSessionId}")
        }.doOnError {
            log.error("Failed to update identification session. useIdSessionId=$useIdSessionId", it)
        }
    }

    fun delete(identificationSession: IdentificationSession): Mono<Void> {
        return identificationSessionRepository.delete(identificationSession)
            .doOnNext {
                log.info("Deleted identification session. useIdSessionId=${identificationSession.useIdSessionId}")
            }.doOnError {
                log.error("Failed to delete identification session. useIdSessionId=${identificationSession.useIdSessionId}", it)
            }
    }
}
