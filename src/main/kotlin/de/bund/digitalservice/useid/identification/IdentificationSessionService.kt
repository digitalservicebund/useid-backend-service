package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val mockDatasource: MockDatasource) {

    fun create(refreshAddress: String, requestAttributes: List<String>): Mono<IdentificationSession> {
        return mockDatasource.create(refreshAddress, requestAttributes)
    }

    fun findByEIDSessionId(eIDSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(mockDatasource.findByEIDSessionId(eIDSessionId))
    }

    fun findByUseIDSessionId(useIDSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(mockDatasource.findByUseIDSessionId(useIDSessionId))
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        return mockDatasource.updateEIDSessionId(useIDSessionId, eIDSessionId)
    }
}
