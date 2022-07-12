package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.datasource.SessionDataSource
import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.model.SessionResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class SessionService(private val sessionDataSource: SessionDataSource) {
    fun createSession(clientRequestSession: ClientRequestSession): Mono<SessionResponse> {
        return Mono.just(
            SessionResponse(clientRequestSession.refreshAddress, UUID.randomUUID().toString())
        ).doOnNext { sessionResponse ->
            sessionDataSource.addSession(sessionResponse)
        }
    }
}
