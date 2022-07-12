package de.bund.digitalservice.useid.datasource

import de.bund.digitalservice.useid.model.CreateIdentitySessionResponse
import org.springframework.stereotype.Repository

@Repository
class IdentificationSessionsDataSource {
    private val sessionStore = mutableListOf<CreateIdentitySessionResponse>()

    fun addSession(createIdentitySessionResponse: CreateIdentitySessionResponse): CreateIdentitySessionResponse {
        sessionStore.add(createIdentitySessionResponse)
        return createIdentitySessionResponse
    }

    fun removeSession(sessionId: String): String {
        sessionStore.removeAt(sessionStore.indexOfFirst { it.sessionId == sessionId })
        return sessionId
    }

    fun getSession(): Collection<CreateIdentitySessionResponse> = sessionStore
}
