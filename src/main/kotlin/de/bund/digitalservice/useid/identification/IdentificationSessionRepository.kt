package de.bund.digitalservice.useid.identification

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime
import java.util.UUID

interface IdentificationSessionRepository : JpaRepository<IdentificationSession, UUID> {
    @Query("SELECT session FROM IdentificationSession session WHERE session.eIdSessionId = :eIdSessionId")
    fun findByEIdSessionId(eIdSessionId: UUID): IdentificationSession?
    fun findByUseIdSessionId(useIdSessionId: UUID): IdentificationSession?
    fun deleteAllByCreatedAtBefore(before: LocalDateTime)

    @Query("DELETE FROM IdentificationSession session WHERE session.eIdSessionId = :eIdSessionId")
    fun deleteByEIdSessionId(eIdSessionId: UUID)
    fun existsByUseIdSessionId(useIdSessionId: UUID): Boolean
}
