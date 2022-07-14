package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Repository

@Repository
class MockDatasource {
    private val sessions: MutableMap<String, IdentificationSession> = mutableMapOf()

    fun save(sessionId: String, tcTokenUrl: String, refreshAddress: String, requestAttributes: List<String>) {
        sessions[sessionId] = IdentificationSession(refreshAddress, requestAttributes, tcTokenUrl)
    }

    fun hasValidSessionId(sessionId: String): Boolean {
        return sessions.containsKey(sessionId)
    }
}
