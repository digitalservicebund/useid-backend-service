package de.bund.digitalservice.useid.identification

import mu.KotlinLogging
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.LocalDateTime.now

private const val RETENTION_IN_DAYS: Long = 7

@Component
class IdentificationSessionRetentionScheduler(val identificationSessionRepository: IdentificationSessionRepository) {
    private val log = KotlinLogging.logger {}

    @Scheduled(cron = "0 0 0 * * *") // every day at midnight
    fun cleanupExpiredIdentificationSessionFromDatabase() {
        log.info("Cleanup identification sessions older than $RETENTION_IN_DAYS days.")
        identificationSessionRepository.deleteAllByCreatedAtBefore(now().minusDays(RETENTION_IN_DAYS))
            .doOnError { log.error("Failed to cleanup expired identification sessions.", it) }
            .doOnNext { log.info("Successfully removed identification sessions older than $RETENTION_IN_DAYS days.") }
            .subscribe()
    }
}
