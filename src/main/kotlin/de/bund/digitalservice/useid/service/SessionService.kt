package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.datasource.SessionDataSource
import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.model.SessionResponse
import de.bund.digitalservice.useid.utils.IdGenerator.Companion.generateUUID
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class SessionService(private val sessionDataSource: SessionDataSource) {
    fun getSession(clientRequestSession: ClientRequestSession): Mono<SessionResponse> {
        return Mono.just(
            SessionResponse(
                clientRequestSession.refreshAddress,
                generateUUID()
            )
        ).doOnNext {
                sessionResponse ->
            sessionDataSource.addSession(sessionResponse)
        }
    }
}
