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
        val availableResults = eidAvailabilityRepository.findAllByUp(true)
        val unavailableResults = eidAvailabilityRepository.findAllByUp(false)

        return unavailableResultsLowEnough(unavailableResults.size, availableResults.size)
    }

    private fun unavailableResultsLowEnough(numberOfUnavailableResults: Int, numberOfAvailableResults: Int): Boolean {
        val numberOfResult = numberOfAvailableResults + numberOfUnavailableResults
        return numberOfResult > 0 && (100.0 * numberOfUnavailableResults / numberOfResult) < applicationProperties.maxPercentageOfEidFailures
    }
}
