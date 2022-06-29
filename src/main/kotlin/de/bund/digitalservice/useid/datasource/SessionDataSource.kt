package de.bund.digitalservice.useid.datasource

import de.bund.digitalservice.useid.model.ClientResponseTCTokenUrl
import org.springframework.stereotype.Repository

@Repository
class SessionDataSource {
    private val sessionStore = mutableListOf<ClientResponseTCTokenUrl>()

    fun addSession(clientResponseTcTokenUrl: ClientResponseTCTokenUrl): ClientResponseTCTokenUrl {
        sessionStore.add(clientResponseTcTokenUrl)
        return clientResponseTcTokenUrl
    }

    fun removeSession(sessionId: String): String {
        sessionStore.removeAt(sessionStore.indexOfFirst { it.sessionId == sessionId })
        return sessionId
    }

    fun getSession(): Collection<ClientResponseTCTokenUrl> = sessionStore
}
