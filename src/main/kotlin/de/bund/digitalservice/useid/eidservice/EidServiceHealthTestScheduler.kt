package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.identification.IdentificationSessionService
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Date

private const val EVERY_MINUTE: String = "* * * * * *"

@Component
class EidServiceHealthTestScheduler(private val eidServiceRepository: EidServiceRepository, private val identificationSessionService: IdentificationSessionService) {

    @Scheduled(cron = EVERY_MINUTE)
    fun checkEIDServiceAvailability() {
        val result = try {
            identificationSessionService.startSession("demo.eid.digitalservicebund.de", listOf("DG4"), "Demo")
            true
        } catch (e: Exception) {
            false
        }
        eidServiceRepository.save(EidServiceHealthDataPoint("${Date()}", result, Date()))
    }
}
