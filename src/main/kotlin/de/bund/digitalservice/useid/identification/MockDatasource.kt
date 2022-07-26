package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.UUID

@Repository
class MockDatasource {
    private val sessions = mutableListOf<IdentificationSession>()

    fun save(tcTokenUrl: String, refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        val currentSessionId = UUID.randomUUID()

        val identificationSession = IdentificationSession(refreshAddress, requestAttributes, currentSessionId, tcTokenUrl)
        sessions.add(identificationSession)

        return Mono.just(identificationSession)
    }

    fun sessionExists(sessionId: UUID): Boolean {
        return sessions.any { it.sessionId == sessionId }
    }
}
