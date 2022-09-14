package de.bund.digitalservice.useid.identification

import org.springframework.data.repository.reactive.ReactiveCrudRepository
import reactor.core.publisher.Mono
import java.util.UUID

interface IdentificationSessionRepository : ReactiveCrudRepository<IdentificationSession, UUID> {
    fun findByEIDSessionId(eIDSessionId: UUID): Mono<IdentificationSession>
    fun findByUseIDSessionId(useIDSessionId: UUID): Mono<IdentificationSession>
}
