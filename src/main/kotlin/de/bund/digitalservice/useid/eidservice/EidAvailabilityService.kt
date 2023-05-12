package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.config.ApplicationProperties
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Date

private const val EVERY_MINUTE: String = "0 * * * * *"

@Component
class EidAvailabilityService(private val eidAvailabilityRepository: EidAvailabilityRepository, private val eidServiceConfig: EidServiceConfig, private val applicationProperties: ApplicationProperties) {

    @Scheduled(cron = EVERY_MINUTE)
    @SchedulerLock(name = "eIdAvailabilityCheck")
    fun checkEidServiceAvailability() {
        val eidService = EidService(eidServiceConfig, listOf("DG4"))
        val result = try {
            eidService.getTcToken("dummyRefreshUrlOnlyForAvailabilityCheck")
            true
        } catch (e: Exception) {
            false
        }
        eidAvailabilityRepository.save(EidAvailabilityCheck("${Date()}", result, Date()))
    }

    internal fun checkFunctionalityOfEidService(): Boolean {
        val lastResults = eidAvailabilityRepository.findAll()
        lastResults.removeAll { it == null } // Remove incorrect data points
        val numberOfDataPoints = lastResults.toList().size
        lastResults.removeAll { it.up }
        val numberOfInvalidResults = lastResults.toList().size

        return invalidResultsLowEnough(numberOfInvalidResults, numberOfDataPoints)
    }

    private fun invalidResultsLowEnough(numberOfInvalidResults: Int, numberOfResults: Int): Boolean {
        return numberOfResults > 0 && (100.0 * numberOfInvalidResults / numberOfResults) < applicationProperties.maxPercentageOfEidFailures
    }
}
