package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val mockDatasource: MockDatasource) {

    fun save(tcTokenUrl: String, refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        return mockDatasource.save(tcTokenUrl, refreshAddress, requestAttributes)
    }

    fun sessionExists(sessionId: UUID): Boolean {
        return mockDatasource.sessionExists(sessionId)
    }
}
