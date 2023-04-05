package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.metrics.METRIC_NAME_EID_INFORMATION
import de.bund.digitalservice.useid.metrics.METRIC_NAME_EID_TCTOKEN
import de.bund.digitalservice.useid.metrics.MetricsService
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class IdentificationSessionService(
    private val identificationSessionRepository: IdentificationSessionRepository,
    private val applicationProperties: ApplicationProperties,
    private val eidServiceConfig: EidServiceConfiguration,
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
    @Transactional
    fun startSession(refreshAddress: String, requestDataGroups: List<String>, tenantID: String): String {
        val session = identificationSessionRepository.save(
            IdentificationSession(UUID.randomUUID(), refreshAddress, requestDataGroups, tenantID),
        )
        log.info("Created new identification session. useIdSessionId=${session.useIdSessionId}")
        return buildTcTokenUrl(session)
    }

    private fun buildTcTokenUrl(session: IdentificationSession): String {
        return "${applicationProperties.baseUrl}$IDENTIFICATION_SESSIONS_BASE_PATH/${session.useIdSessionId}/$TCTOKEN_PATH_SUFFIX"
    }

    @Transactional
    fun startSessionWithEIdServer(useIdSessionId: UUID): TCTokenType {
        val identificationSession = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
            ?: throw IdentificationSessionNotFoundException(useIdSessionId)
        val tcToken: TCTokenType
        val tenantID = identificationSession.tenantId ?: ""
        try {
            val eidService = EidService(eidServiceConfig, identificationSession.requestDataGroups)
            tcToken = eidService.getTcToken("${applicationProperties.baseUrl}$REFRESH_PATH")
            metricsService.incrementSuccessCounter(METRIC_NAME_EID_TCTOKEN, tenantID)
        } catch (e: Exception) {
            metricsService.incrementErrorCounter(METRIC_NAME_EID_TCTOKEN, tenantID)
            log.error("Failed to get tc token for identification session. useIdSessionId=$useIdSessionId", e)
            throw e
        }
        updateEIdSessionId(useIdSessionId, extractEIdSessionId(tcToken))
        return tcToken
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

    fun updateEIdSessionId(useIdSessionId: UUID, eIdSessionId: UUID): IdentificationSession {
        val session = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
        session!!.eIdSessionId = eIdSessionId
        identificationSessionRepository.save(session)
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")
        return session
    }

    @Transactional
    fun getIdentity(
        eIdSessionId: UUID,
        tenantId: String,
    ): GetResultResponseType {
        val eidInformation = getEidInformation(eIdSessionId, tenantId)

        // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
        if (eidInformation.result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
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
    ): GetResultResponseType {
        val result: GetResultResponseType
        try {
            val eidService = EidService(eidServiceConfig)
            result = eidService.getEidInformation(eIdSessionId.toString())
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
