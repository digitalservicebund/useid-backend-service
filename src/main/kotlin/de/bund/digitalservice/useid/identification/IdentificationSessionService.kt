package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid240.GetResultResponse
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.config.METRIC_NAME_EID_SERVICE_REQUESTS
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import de.governikus.panstar.sdk.soap.handler.TcTokenWrapper
import de.governikus.panstar.sdk.tctoken.TCTokenType
import de.governikus.panstar.sdk.utils.RequestData
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class IdentificationSessionService(
    private val identificationSessionRepository: IdentificationSessionRepository,
    private val applicationProperties: ApplicationProperties,
    private val soapHandler: SoapHandler,
) {

    private val log = KotlinLogging.logger {}

    private val tcTokenCallsSuccessfulCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_tc_token", "status", "200")
    private val tcTokenCallsWithErrorsCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_tc_token", "status", "500")
    private val getEidInformationCallsSuccessfulCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "200")
    private val getEidInformationCallsWithErrorsCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "500")

    /**
     * Starting a new identification session
     *
     * @param refreshAddress the refresh address to be returned to after finishing the session
     * @param requestDataGroups the data being requested for that identification session
     * @return TC token URL for the started session
     */
    @Transactional
    fun startSession(refreshAddress: String, requestDataGroups: List<String>): String {
        val session = identificationSessionRepository.save(
            IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups),
        )
        log.info("Created new identification session. useIdSessionId=${session.useIdSessionId}")
        return buildTcTokenUrl(session)
    }

    private fun buildTcTokenUrl(session: IdentificationSession): String {
        return "${applicationProperties.baseUrl}$IDENTIFICATION_SESSIONS_BASE_PATH/${session.useIdSessionId}/$TCTOKEN_PATH_SUFFIX"
    }

    @Transactional
    fun startSessionWithEIdServer(useIdSessionId: UUID): TCTokenType {
        val requestData: RequestData = getRequestData(useIdSessionId)

        val tcTokenWrapper: TcTokenWrapper?
        try {
            tcTokenWrapper = soapHandler.getTcToken(requestData, "${applicationProperties.baseUrl}$REFRESH_PATH")
            tcTokenCallsSuccessfulCounter.increment()
        } catch (e: Exception) {
            tcTokenCallsWithErrorsCounter.increment()
            log.error("Failed to get tc token for identification session. useIdSessionId=$useIdSessionId", e)
            throw e
        }

        updateEIdSessionId(useIdSessionId, extractEIdSessionId(tcTokenWrapper.tcToken))

        return tcTokenWrapper.tcToken
    }

    private fun getRequestData(useIdSessionId: UUID): RequestData {
        val identificationSession = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
            ?: throw IdentificationSessionNotFoundException(useIdSessionId)

        val requestData = RequestData()

        identificationSession.requestDataGroups.forEach {
            when (it) {
                "DG1" -> requestData.documentType(true)
                "DG2" -> requestData.issuingState(true)
                "DG3" -> requestData.dateOfExpiry(true)
                "DG4" -> requestData.givenNames(true)
                "DG5" -> requestData.familyNames(true)
                "DG7" -> requestData.academicTitle(true)
                "DG8" -> requestData.dateOfBirth(true)
                "DG9" -> requestData.placeOfBirth(true)
                "DG10" -> requestData.nationality(true)
                "DG13" -> requestData.birthName(true)
                "DG17" -> requestData.placeOfResidence(true)
                "DG19" -> requestData.residencePermitI(true)
                else -> {
                    throw IllegalStateException("Invalid data group for this eService")
                }
            }
        }

        return requestData
    }

    @Transactional
    fun getIdentity(
        eIdSessionId: UUID,
    ): GetResultResponse {
        val userData: GetResultResponse?
        try {
            userData = soapHandler.getResult(eIdSessionId.toString())
            getEidInformationCallsSuccessfulCounter.increment()
        } catch (e: Exception) {
            getEidInformationCallsWithErrorsCounter.increment()
            log.error("Failed to fetch identity data: ${e.message}.")
            throw e
        }

        // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
        if (userData.result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
            try {
                identificationSessionRepository.deleteByEIdSessionId(eIdSessionId)
                log.info("Deleted identification session.")
            } catch (e: Exception) {
                log.error("Failed to delete identification session.", e)
            }
        } else {
            // resultMinor error codes can be found in TR 03130 Part 1 -> 3.4.1 Error Codes
            log.info("The resultMinor for identification session is ${userData.result.resultMinor}.")
        }
        return userData
    }

    private fun extractEIdSessionId(tcToken: TCTokenType): UUID {
        val eIdSessionId = UriComponentsBuilder
            .fromHttpUrl(tcToken.refreshAddress)
            .encode().build()
            .queryParams.getFirst("sessionId")
        return UUID.fromString(eIdSessionId)
    }

    fun findByEIdSessionId(eIdSessionId: UUID): IdentificationSession? {
        return identificationSessionRepository.findByEIdSessionId(eIdSessionId)
    }

    private fun updateEIdSessionId(useIdSessionId: UUID, eIdSessionId: UUID) {
        val session = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
        session!!.eIdSessionId = eIdSessionId
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")
    }
}
