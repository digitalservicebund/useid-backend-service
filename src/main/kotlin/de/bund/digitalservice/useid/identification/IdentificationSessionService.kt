package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService(private val mockDatasource: MockDatasource) {

    fun create(refreshAddress: String, requestDataGroups: List<String>): Mono<IdentificationSession> {
        return mockDatasource.create(refreshAddress, requestDataGroups)
    }

    fun findByEIDSessionId(eIDSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(mockDatasource.findByEIDSessionId(eIDSessionId))
    }

    fun findByUseIDSessionId(useIDSessionId: UUID): Mono<IdentificationSession> {
        return Mono.justOrEmpty(mockDatasource.findByUseIDSessionId(useIDSessionId))
    }

    fun findByEIDSessionIdOrFail(eIDSessionId: UUID): IdentificationSession {
        return mockDatasource.findByEIDSessionId(eIDSessionId)
            ?: throw NoSuchElementException()
    }

    fun updateEIDSessionId(useIDSessionId: UUID, eIDSessionId: UUID) {
        return mockDatasource.updateEIDSessionId(useIDSessionId, eIDSessionId)
    }

    fun delete(session: IdentificationSession) {
        mockDatasource.delete(session)
    }
}
