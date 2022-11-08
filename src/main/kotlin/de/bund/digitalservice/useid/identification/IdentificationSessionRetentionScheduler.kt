package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

internal const val RETENTION_IN_DAYS: Long = 7
private const val EVERY_DAY_AT_MIDNIGHT: String = "0 0 0 * * *"

@Component
class IdentificationSessionRetentionScheduler(val identificationSessionRepository: IdentificationSessionRepository) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = EVERY_DAY_AT_MIDNIGHT)
    @SchedulerLock(name = "identificationSessionRetentionCleanup")
    fun cleanupExpiredIdentificationSessionFromDatabase() {
        log.info("Cleanup identification sessions older than $RETENTION_IN_DAYS days.")
        identificationSessionRepository.deleteAllByCreatedAtBefore(now().minusDays(RETENTION_IN_DAYS))
            .doOnError { log.error("Failed to cleanup expired identification sessions.", it) }
            .doOnSuccess { log.info("Successfully removed identification sessions older than $RETENTION_IN_DAYS days.") }
            .subscribe()
    }
}
