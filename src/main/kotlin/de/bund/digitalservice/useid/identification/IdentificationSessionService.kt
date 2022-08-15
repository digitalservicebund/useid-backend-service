package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val mockDatasource: MockDatasource) {

    fun create(refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        return mockDatasource.create(refreshAddress, requestAttributes)
    }

    fun sessionExists(useIDSessionId: UUID): Boolean {
        return mockDatasource.sessionExists(useIDSessionId)
    }

    fun findByIdOrFail(useIDSessionId: UUID): Mono<IdentificationSession> {
        return Mono.just(mockDatasource.findByIdOrFail(useIDSessionId))
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        return mockDatasource.updateEIDSessionId(useIDSessionId, eIDSessionId)
    }
}
