package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class IdentificationSessionService(private val identificationSessionRepository: IdentificationSessionRepository) {

    private val log = KotlinLogging.logger {}

    fun create(refreshAddress: String, requestDataGroups: List<String>): IdentificationSession {
        val session = identificationSessionRepository.save(
            IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups)
        )
        log.info("Created new identification session. useIdSessionId=${session.useIdSessionId}")
        return session
    }

    fun findByEIDSessionId(eIdSessionId: UUID): IdentificationSession? {
        return identificationSessionRepository.findByEIdSessionId(eIdSessionId)
    }

    fun findByUseIdSessionId(useIdSessionId: UUID): IdentificationSession? {
        return identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
    }

    fun updateEIDSessionId(useIdSessionId: UUID, eIdSessionId: UUID): IdentificationSession {
        val session = findByUseIdSessionId(useIdSessionId)
        session!!.eIdSessionId = eIdSessionId
        identificationSessionRepository.save(session)
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")
        return session
    }

    fun delete(identificationSession: IdentificationSession) {
        identificationSessionRepository.delete(identificationSession)
        log.info("Deleted identification session. useIdSessionId=${identificationSession.useIdSessionId}")
    }
}
