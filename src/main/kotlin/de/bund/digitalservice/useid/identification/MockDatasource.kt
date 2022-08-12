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

    fun findByIdOrFail(useIDSessionId: UUID): Mono<IdentificationSession> {
        if (!sessionExists(useIDSessionId)) {
            throw Error("no session found with useIDSessionId $useIDSessionId")
        }
        return Mono.just(sessions.find { it.useIDSessionId == useIDSessionId }!!)
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        // use !! here bc findByIdOrFail was already executed
        val session = sessions.find {
            it.useIDSessionId == useIDSessionId
        }!!
        session.eIDSessionId = eIDSessionId
        log.info { "set new eIDSessionId: ${session.eIDSessionId} for session with useIDSessionId: $useIDSessionId" }
    }
}
