package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Date

private const val EVERY_MINUTE: String = "* * * * * *"

@Component
class EidServiceHealthTestScheduler(private val eidServiceRepository: EidServiceRepository, private val eidServiceConfig: EidServiceConfig, private val applicationProperties: ApplicationProperties) {

    @Scheduled(cron = EVERY_MINUTE)
    @SchedulerLock(name = "eIdServiceHealtCheck")
    fun checkEidServiceAvailability() {
        val eidService = EidService(eidServiceConfig, listOf("DG4"))
        val result = try {
            eidService.getTcToken("${applicationProperties.baseUrl}$REFRESH_PATH")
            true
        } catch (e: Exception) {
            false
        }
        eidServiceRepository.save(EidServiceHealthDataPoint("${Date()}", result, Date()))
    }
}
