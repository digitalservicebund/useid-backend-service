package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.PostgresDialect
import org.springframework.r2dbc.core.DatabaseClient
import reactor.test.StepVerifier
import java.time.LocalDateTime.now
import java.util.UUID

@SpringBootTest
internal class IdentificationSessionRetentionSchedulerIntegrationTest : PostgresTestcontainerIntegrationTest() {
    @Autowired
    private lateinit var repository: IdentificationSessionRepository

    @Autowired
    private lateinit var scheduler: IdentificationSessionRetentionScheduler

    @Autowired
    private lateinit var client: DatabaseClient
    private lateinit var template: R2dbcEntityTemplate

    @BeforeAll
    fun setup() {
        template = R2dbcEntityTemplate(client, PostgresDialect.INSTANCE)
    }

    @Test
    fun `Cleanup successfully removes expired identification sessions from database`() {
        // Given
        val useIdSessionId = UUID.randomUUID()
        val identificationSession = IdentificationSession(useIdSessionId, "some-refresh-address", emptyList())
        identificationSession.createdAt = now().minusDays(RETENTION_IN_DAYS).minusDays(1)
        template.insert(identificationSession).then().`as`(StepVerifier::create).verifyComplete()

        // When
        scheduler.cleanupExpiredIdentificationSessionFromDatabase()

        // Then
        repository.findByUseIdSessionId(useIdSessionId)
            .`as`(StepVerifier::create)
            .expectNextCount(0)
            .verifyComplete()
    }
}
