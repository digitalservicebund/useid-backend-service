package de.bund.digitalservice.useid.identification

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.time.LocalDateTime
import java.util.UUID

interface IdentificationSessionRepository : ReactiveCrudRepository<IdentificationSession, UUID> {
    fun findById(id: Long): Mono<IdentificationSession>
    fun findByEidSessionId(eIDSessionId: UUID): Mono<IdentificationSession>
    fun findByUseidSessionId(useIDSessionId: UUID): Mono<IdentificationSession>
    fun deleteAllByCreatedAtBefore(before: LocalDateTime): Mono<Void>
}
