package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.metrics.METRIC_NAME_EID_TCTOKEN
import de.bund.digitalservice.useid.metrics.MetricsService
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import mu.KotlinLogging
import org.springframework.stereotype.Service
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

    fun startSessionWithEIdServer(useIdSessionId: UUID): TCTokenType {
        val identificationSession = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
            ?: throw IdentificationSessionNotFoundException(useIdSessionId)
        val tcToken: TCTokenType
        val tenantID = identificationSession.tenantId ?: ""
        try {
            val eidService = EidService(eidServiceConfig, identificationSession.requestDataGroups)
            tcToken = eidService.getTcToken("${applicationProperties.baseUrl}$REFRESH_PATH")
            metricsService.incrementCounter(METRIC_NAME_EID_TCTOKEN, "200", tenantID)
        } catch (e: Exception) {
            metricsService.incrementCounter(METRIC_NAME_EID_TCTOKEN, "500", tenantID)
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

    fun delete(identificationSession: IdentificationSession) {
        identificationSessionRepository.delete(identificationSession)
        log.info("Deleted identification session. useIdSessionId=${identificationSession.useIdSessionId}")
    }
}
