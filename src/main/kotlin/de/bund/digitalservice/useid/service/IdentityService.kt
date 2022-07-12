package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.datasource.IdentificationSessionsDataSource
import de.bund.digitalservice.useid.model.IdentityAttributes
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.switchIfEmpty

@Service
class IdentityService(private val identificationSessionsDataSource: IdentificationSessionsDataSource) {
    fun getIdentity(sessionId: String): Mono<IdentityAttributes> {
        return Mono.just(IdentityAttributes("firstname", "lastname"))
            .filter {
                identificationSessionsDataSource.getSession().any { it.sessionId == sessionId }
            }.doOnNext {
                identificationSessionsDataSource.removeSession(sessionId)
            }.switchIfEmpty {
                Mono.error { throw NoSuchElementException("Error: sessionId is not found") }
            }
    }
}
