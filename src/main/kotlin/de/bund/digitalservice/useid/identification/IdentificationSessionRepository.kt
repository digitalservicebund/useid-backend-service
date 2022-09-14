package de.bund.digitalservice.useid.identification

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface IdentificationSessionRepository : ReactiveCrudRepository<IdentificationSession, UUID> {
    @Query("select * from identification_session where eid_session_id = $1")
    fun findByEIDSessionId(eIDSessionId: UUID): Mono<IdentificationSession>
}
