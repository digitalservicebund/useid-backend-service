package de.bund.digitalservice.useid.identification

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime.now
import java.util.UUID

@SpringBootTest
@Transactional
@Tag("integration")
internal class IdentificationSessionRetentionSchedulerIntegrationTest {
    @Autowired
    private lateinit var repository: IdentificationSessionRepository

    @Autowired
    private lateinit var scheduler: IdentificationSessionRetentionScheduler

    @Test
    fun `Cleanup successfully removes expired identification sessions from database`() {
        // Given
        val useIdSessionId = UUID.randomUUID()
        val identificationSession = IdentificationSession(useIdSessionId, "some-refresh-address", emptyList(), "testTenant")
        identificationSession.createdAt = now().minusDays(RETENTION_IN_DAYS).minusDays(1)
        repository.save(identificationSession)

        // When
        scheduler.cleanupExpiredIdentificationSessionFromDatabase()

        // Then
        repository.findByUseIdSessionId(useIdSessionId)
    }
}
