package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val identificationSessionRepository: IdentificationSessionRepository) {

    private val log = KotlinLogging.logger {}

    fun create(refreshAddress: String, requestDataGroups: List<String>): Mono<IdentificationSession> {
        return identificationSessionRepository.save(
            IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups)
        ).doOnNext {
            log.info("Created new identification session. useIDSessionId=${it.useidSessionId}")
        }
    }

    fun findByEIDSessionId(eIDSessionId: UUID): Mono<IdentificationSession> {
        return identificationSessionRepository.findByEidSessionId(eIDSessionId)
    }

    fun findByUseIDSessionId(useIDSessionId: UUID): Mono<IdentificationSession> {
        return identificationSessionRepository.findByUseidSessionId(useIDSessionId)
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID): Mono<IdentificationSession> {
        return findByUseIDSessionId(useIDSessionId).flatMap {
            it.eidSessionId = eIDSessionId
            identificationSessionRepository.save(it)
        }.doOnNext {
            log.info("Updated eIDSessionId of identification session. useIDSessionId=${it.useidSessionId}, eIDSessionId=${it.eidSessionId}")
        }.doOnError {
            log.error("Failed to update identification session. useIDSessionId=$useIDSessionId", it)
        }
    }

    fun delete(identificationSession: IdentificationSession): Mono<Void> {
        return identificationSessionRepository.delete(identificationSession)
            .doOnNext {
                log.info("Deleted identification session. useIDSessionId=${identificationSession.useidSessionId}")
            }.doOnError {
                log.error("Failed to delete identification session. useIDSessionId=${identificationSession.useidSessionId}", it)
            }
    }
}
