package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val mockDatasource: MockDatasource) {

    fun save(refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        return mockDatasource.save(refreshAddress, requestAttributes)
    }

    fun sessionExists(useIDSessionId: UUID): Boolean {
        return mockDatasource.sessionExists(useIDSessionId)
    }

    fun findByIdOrFail(useIDSessionId: UUID): Mono<IdentificationSession> {
        return mockDatasource.findByIdOrFail(useIDSessionId)
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        return mockDatasource.updateEIDSessionId(useIDSessionId, eIDSessionId)
    }
}
