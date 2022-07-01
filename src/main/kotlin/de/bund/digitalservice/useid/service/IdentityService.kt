package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.datasource.SessionDataSource
import de.bund.digitalservice.useid.model.IdentityAttributes
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class IdentityService(private val sessionDataSource: SessionDataSource) {
    fun getIdentity(sessionId: String): Mono<IdentityAttributes> {
        return Mono.just(IdentityAttributes("firstname", "lastname"))
            .filter {
                sessionDataSource
                    .getSession()
                    .any {
                        it.sessionId == sessionId
                    }
            }.doOnNext {
                sessionDataSource.removeSession(sessionId)
            }.switchIfEmpty {
                Mono.error {
                    throw NoSuchElementException("Error: sessionId is not found")
                }
            }
    }
}
