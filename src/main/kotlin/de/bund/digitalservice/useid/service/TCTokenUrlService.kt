package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.datasource.SessionDataSource
import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.model.ClientResponseTCTokenUrl
import de.bund.digitalservice.useid.utils.IdGenerator.Companion.generateUUID
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TCTokenUrlService(private val sessionDataSource: SessionDataSource) {
    fun getTCTokenUrl(clientRequestSession: ClientRequestSession): Mono<ClientResponseTCTokenUrl> {
        return Mono.just(
            ClientResponseTCTokenUrl(
                clientRequestSession.refreshAddress,
                generateUUID()
            )
        ).doOnNext {
                tcTokenUrl ->
            sessionDataSource.addSession(tcTokenUrl)
        }
    }
}
