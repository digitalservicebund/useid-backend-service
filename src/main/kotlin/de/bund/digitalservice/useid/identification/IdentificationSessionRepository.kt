package de.bund.digitalservice.useid.identification

import org.springframework.data.r2dbc.repository.Query
import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

interface IdentificationSessionRepository : ReactiveCrudRepository<IdentificationSession, UUID> {
    fun findById(id: Long): Mono<IdentificationSession>

    @Query("SELECT * FROM identification_session WHERE eid_session_id = :eIdSessionId")
    fun findByEIdSessionId(eIdSessionId: UUID): Mono<IdentificationSession>
    fun findByUseIdSessionId(useIdSessionId: UUID): Mono<IdentificationSession>
    fun deleteAllByCreatedAtBefore(before: LocalDateTime): Mono<Void>
}
