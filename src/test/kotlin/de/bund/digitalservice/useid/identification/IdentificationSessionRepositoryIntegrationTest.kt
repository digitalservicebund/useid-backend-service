package de.bund.digitalservice.useid.identification

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.time.LocalDateTime
import java.util.UUID

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Tag("integration")
class IdentificationSessionRepositoryIntegrationTest {
    companion object {
        private const val REFRESH_ADDRESS: String = "some-refresh-address"
        private const val DG1 = "DG1"
        private const val DG2 = "DG2"
        private val DATA_GROUPS: List<String> = listOf(DG1, DG2)
        private val USEID_SESSION_ID: UUID = UUID.randomUUID()
        private val EID_SESSION_ID: UUID = UUID.randomUUID()
        private const val TENANT_ID: String = "TestTenant"
    }

    @BeforeAll
    fun cleanupDatabase() {
        identificationSessionRepository.deleteAll()
    }

    @Autowired
    private lateinit var identificationSessionRepository: IdentificationSessionRepository

    @Test
    fun `find identification session by useIdSessionId and eIdSessionId returns the inserted entity`() {
        // Given
        val identificationSession = IdentificationSession(USEID_SESSION_ID, REFRESH_ADDRESS, DATA_GROUPS, TENANT_ID)
        identificationSession.eIdSessionId = EID_SESSION_ID

        // When
        identificationSessionRepository.save(identificationSession)

        // Then
        validateIdentificationSession(identificationSessionRepository.findByUseIdSessionId(USEID_SESSION_ID))
        validateIdentificationSession(identificationSessionRepository.findByEIdSessionId(EID_SESSION_ID))
    }

    @Test
    fun `deleteAllByCreatedAtBefore removes expired entities successfully`() {
        // Given
        val now = LocalDateTime.now()
        val deleteBefore = now.minusDays(7)

        // To be deleted
        val createdLongBeforeExpiry = insertNewIdentificationSession(now.minusDays(100))
        val createdDayBeforeExpiry = insertNewIdentificationSession(now.minusDays(8))
        val createdRightBeforeExpiry = insertNewIdentificationSession(now.minusDays(7).minusSeconds(1))

        // To be kept
        val createdExactlyOnExpiry = insertNewIdentificationSession(now.minusDays(7))
        val createdDayAfterExpiry = insertNewIdentificationSession(now.minusDays(6))
        val createdLongAfterExpiry = insertNewIdentificationSession(now.minusDays(1))
        val createdNow = insertNewIdentificationSession(now)

        // When
        identificationSessionRepository.deleteAllByCreatedAtBefore(deleteBefore)

        // Then
        verifyIdentificationSessionWasDeleted(createdLongBeforeExpiry)
        verifyIdentificationSessionWasDeleted(createdDayBeforeExpiry)
        verifyIdentificationSessionWasDeleted(createdRightBeforeExpiry)

        verifyIdentificationSessionExists(createdExactlyOnExpiry)
        verifyIdentificationSessionExists(createdDayAfterExpiry)
        verifyIdentificationSessionExists(createdLongAfterExpiry)
        verifyIdentificationSessionExists(createdNow)
    }

    private fun verifyIdentificationSessionWasDeleted(session: IdentificationSession) {
        val result = identificationSessionRepository.findByUseIdSessionId(session.useIdSessionId!!)
        assertEquals(null, result)
    }

    private fun verifyIdentificationSessionExists(session: IdentificationSession) {
        val result = identificationSessionRepository.findByUseIdSessionId(session.useIdSessionId!!)
        assertEquals(session.id, result?.id)
    }

    fun insertNewIdentificationSession(createdAt: LocalDateTime): IdentificationSession {
        val identificationSession = IdentificationSession(UUID.randomUUID(), REFRESH_ADDRESS, DATA_GROUPS, TENANT_ID)

        val savedSession = identificationSessionRepository.save(identificationSession)
        savedSession.createdAt = createdAt

        return savedSession
    }

    private fun validateIdentificationSession(identificationSession: IdentificationSession?) {
        assertThat(identificationSession?.id)
            .isNotNull
        assertThat(identificationSession?.eIdSessionId)
            .isEqualTo(EID_SESSION_ID)
        assertThat(identificationSession?.useIdSessionId)
            .isEqualTo(USEID_SESSION_ID)
        assertThat(identificationSession?.refreshAddress)
            .isEqualTo(REFRESH_ADDRESS)
        assertThat(identificationSession?.requestDataGroups)
            .contains(DG1, DG2)
    }
}
