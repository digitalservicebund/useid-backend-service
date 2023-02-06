package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val identificationSessionRepository: IdentificationSessionRepository) {

    private val log = KotlinLogging.logger {}

    fun create(refreshAddress: String, requestDataGroups: List<String>): Mono<IdentificationSession> {
        return Mono.just(IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups))
            .doOnNext {
                log.info("Created new identification session. useIdSessionId=${it.useIdSessionId}")
            }.doOnError {
                log.error("Failed to create identification session: ${it.message}")
            }
    }

    fun findByEIDSessionId(eIdSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(identificationSessionRepository.findByEIdSessionId(eIdSessionId))
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(identificationSessionRepository.findByUseIdSessionId(useIdSessionId))
    }

    fun updateEIDSessionId(useIdSessionId: UUID, eIdSessionId: UUID): Mono<IdentificationSession> {
        val session = findByUseIdSessionId(useIdSessionId).block()!!
        session.eIdSessionId = eIdSessionId
        identificationSessionRepository.save(session)
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")
        return Mono.just(session)
    }

    fun delete(identificationSession: IdentificationSession): Mono<Void> {
        identificationSessionRepository.delete(identificationSession)
        log.info("Deleted identification session. useIdSessionId=${identificationSession.useIdSessionId}")
        return Mono.empty()
    }
}
