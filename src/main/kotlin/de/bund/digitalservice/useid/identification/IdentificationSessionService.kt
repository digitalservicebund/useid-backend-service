package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid240.GetResultResponse
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.metrics.METRIC_NAME_EID_INFORMATION
import de.bund.digitalservice.useid.metrics.METRIC_NAME_EID_TCTOKEN
import de.bund.digitalservice.useid.metrics.MetricsService
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import de.governikus.panstar.sdk.tctoken.TCTokenType
import de.governikus.panstar.sdk.utils.RequestData
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class IdentificationSessionService(
    private val identificationSessionRepository: IdentificationSessionRepository,
    private val applicationProperties: ApplicationProperties,
    private val soapHandler: SoapHandler,
    private val metricsService: MetricsService,
) {

    private val log = KotlinLogging.logger {}

    /**
     * Starting a new identification session
     *
     * @param refreshAddress the refresh address to be returned to after finishing the session
     * @param requestDataGroups the data being requested for that identification session
     * @param tenantID the id of the tenant that is starting the identification session
     * @return TC token URL for the started session
     */
    fun startSession(refreshAddress: String, requestDataGroups: List<String>, tenantID: String): String {
        val session = identificationSessionRepository.save(
            IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups, tenantID),
        )
        log.info("Created new identification session. useIdSessionId=${session.useIdSessionId}")
        return buildTcTokenUrl(session)
    }

    private fun buildTcTokenUrl(session: IdentificationSession): String {
        return "${applicationProperties.baseUrl}$TCTOKENS_BASE_PATH/${session.useIdSessionId}"
    }

    fun startSessionWithEIdServer(useIdSessionId: UUID): TCTokenType {
        val session = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
            ?: throw IdentificationSessionNotFoundException(useIdSessionId)
        val tenantID = session.tenantId ?: ""

        val requestData: RequestData = getRequestData(useIdSessionId)
        val tcToken: TCTokenType
        try {
            tcToken = soapHandler.getTcToken(requestData, "${applicationProperties.baseUrl}$REFRESH_PATH").tcToken
            metricsService.incrementSuccessCounter(METRIC_NAME_EID_TCTOKEN, tenantID)
        } catch (e: Exception) {
            metricsService.incrementErrorCounter(METRIC_NAME_EID_TCTOKEN, tenantID)
            log.error("Failed to get tc token for identification session. useIdSessionId=$useIdSessionId")
            throw e
        }
        session.eIdSessionId = extractEIdSessionId(tcToken)
        identificationSessionRepository.save(session)
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")

        return tcToken
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
                else -> error("Invalid data group for this eService")
            }
        }

        return requestData
    }

    private fun extractEIdSessionId(tcToken: TCTokenType): UUID {
        val eIdSessionId = UriComponentsBuilder
            .fromHttpUrl(tcToken.refreshAddress)
            .encode().build()
            .queryParams.getFirst("sessionId")
        return UUID.fromString(eIdSessionId)
    }

    fun findByEIdSessionIdOrThrow(eIdSessionId: UUID): IdentificationSession {
        return identificationSessionRepository.findByEIdSessionId(eIdSessionId)
            ?: throw IdentificationSessionNotFoundException()
    }

    fun getIdentity(
        eIdSessionId: UUID,
        tenantId: String,
    ): GetResultResponse {
        val eidInformation = getEidInformation(eIdSessionId, tenantId)

        // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
        if (eidInformation.result.resultMajor == "http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok") {
            deleteSession(eIdSessionId)
        } else {
            // resultMinor error codes can be found in TR 03130 Part 1 -> 3.4.1 Error Codes
            log.info("The resultMinor for identification session is ${eidInformation.result.resultMinor}.")
        }
        return eidInformation
    }

    private fun getEidInformation(
        eIdSessionId: UUID,
        tenantId: String,
    ): GetResultResponse {
        val result: GetResultResponse
        try {
            result = soapHandler.getResult(eIdSessionId.toString())
            metricsService.incrementSuccessCounter(METRIC_NAME_EID_INFORMATION, tenantId)
        } catch (e: Exception) {
            metricsService.incrementErrorCounter(METRIC_NAME_EID_INFORMATION, tenantId)
            log.error("Failed to fetch identity data: ${e.message}")
            throw e
        }
        return result
    }

    private fun deleteSession(eIdSessionId: UUID) {
        try {
            identificationSessionRepository.deleteByEIdSessionId(eIdSessionId)
            log.info("Deleted identification session.")
        } catch (e: Exception) {
            log.error("Failed to delete identification session.")
        }
    }
}
