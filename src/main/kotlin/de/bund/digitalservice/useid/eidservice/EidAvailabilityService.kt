package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import de.governikus.panstar.sdk.utils.RequestData
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.util.Date

private const val EVERY_MINUTE: String = "0 * * * * *"

@Component
class EidAvailabilityService(
    private val eidAvailabilityRepository: EidAvailabilityRepository,
    private val soapHandler: SoapHandler,
    private val applicationProperties: ApplicationProperties,
) {

    @Scheduled(cron = EVERY_MINUTE)
    @SchedulerLock(name = "eIdAvailabilityCheck")
    fun checkEidServiceAvailability() {
        val result = try {
            soapHandler.getTcToken(RequestData(), "dummyRefreshUrlOnlyForAvailabilityCheck").tcToken
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
