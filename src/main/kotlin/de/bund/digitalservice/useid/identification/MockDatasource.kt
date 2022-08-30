package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class MockDatasource {
    private val sessions = mutableListOf<IdentificationSession>()
    private val log = KotlinLogging.logger {}

    fun create(refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        val useIDSessionId = UUID.randomUUID()

        val identificationSession = IdentificationSession(
            refreshAddress = refreshAddress,
            requestAttributes = requestAttributes,
            useIDSessionId = useIDSessionId
        )
        sessions.add(identificationSession)

        return Mono.just(identificationSession)
    }

    fun sessionExists(useIDSessionId: UUID): Boolean {
        return sessions.any { it.useIDSessionId == useIDSessionId }
    }

    fun findByUseIDSessionId(useIDSessionId: UUID): IdentificationSession? {
        return sessions.find { it.useIDSessionId == useIDSessionId }
    }

    fun findByEIDSessionId(eIDSessionId: UUID): IdentificationSession? {
        return sessions.find { it.eIDSessionId == eIDSessionId }
    }

    fun findByIdOrFail(useIDSessionId: UUID): IdentificationSession {
        if (!sessionExists(useIDSessionId)) {
            throw Error("no session found with useIDSessionId $useIDSessionId")
        }
        return sessions.find { it.useIDSessionId == useIDSessionId }!!
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        val session = findByIdOrFail(useIDSessionId)
        session.eIDSessionId = eIDSessionId
        log.info { "set new eIDSessionId: ${session.eIDSessionId} for session with useIDSessionId: $useIDSessionId" }
    }

    fun delete(session: IdentificationSession): Boolean {
        if (sessions.remove(session)) {
            log.info { "deleted session with useIdSessionId ${session.useIDSessionId}" }
            return true
        }
        return false
    }
}
