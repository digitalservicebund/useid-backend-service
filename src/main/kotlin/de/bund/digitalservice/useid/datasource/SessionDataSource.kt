package de.bund.digitalservice.useid.datasource

import de.bund.digitalservice.useid.model.SessionResponse
import org.springframework.stereotype.Repository

@Repository
class SessionDataSource {
    private val sessionStore = mutableListOf<SessionResponse>()

    fun addSession(sessionResponse: SessionResponse): SessionResponse {
        sessionStore.add(sessionResponse)
        return sessionResponse
    }

    fun removeSession(sessionId: String): String {
        sessionStore.removeAt(sessionStore.indexOfFirst { it.sessionId == sessionId })
        return sessionId
    }

    fun getSession(): Collection<SessionResponse> = sessionStore
}
