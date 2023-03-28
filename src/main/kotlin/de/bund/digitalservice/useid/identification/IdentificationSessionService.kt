package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid240.GetResultResponse
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.config.METRIC_NAME_EID_SERVICE_REQUESTS
import de.bund.digitalservice.useid.refresh.REFRESH_PATH
import de.governikus.identification.report.constants.SchemaConstants
import de.governikus.panstar.sdk.soap.configuration.SoapConfiguration
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import de.governikus.panstar.sdk.soap.handler.TcTokenWrapper
import de.governikus.panstar.sdk.tctoken.TCTokenType
import de.governikus.panstar.sdk.utils.RequestData
import de.governikus.panstar.sdk.utils.constant.LevelOfAssuranceType
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class IdentificationSessionService(
    private val identificationSessionRepository: IdentificationSessionRepository,
    private val applicationProperties: ApplicationProperties,
    private val soapConfiguration: SoapConfiguration,
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

    private var soapHandler: SoapHandler? = null

    /**
     * Starting a new identification session
     *
     * @param refreshAddress the refresh address to be returned to after finishing the session
     * @param requestDataGroups the data being requested for that identification session
     * @return TC token URL for the started session
     */
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

    fun startSessionWithEIdServer(useIdSessionId: UUID): TCTokenType {
        // TODO read request data from identification session
        val requestData: RequestData = RequestData().documentType(true)
            .issuingState(false)
            .dateOfExpiry(true)
            .givenNames(false)
            .familyNames(true)
            .artisticName(false)
            .academicTitle(true)
            .dateOfBirth(false)
            .placeOfBirth(true)
            .nationality(false)
            .birthName(true)
            .placeOfResidence(false)
            .communityID(true)
            .residencePermitI(false)
            .restrictedID(true)
            .ageVerification(false, 18)
            .placeVerification(true, "02760400110000")
            .transactionInfo("This is a demo transaction info.")
            .levelOfAssurance(LevelOfAssuranceType.BUND_NORMAL)
            .seCertified(true)
            .seEndorsed(false)
            .hwKeyStore(true)
            .cardCertified(true)
            .transactionAttestation(
                SchemaConstants.Ids.IDENTIFICATION_REPORT_2_0_ID,
                "{\"subjectRefType\": \"${SchemaConstants.Ids.FINK_PERSON_REF_MINIMAL_ID}\"}",
            )

        val tcTokenWrapper: TcTokenWrapper?
        try {
            tcTokenWrapper = getSoapHandler().getTcToken(requestData, "${applicationProperties.baseUrl}$REFRESH_PATH")
            tcTokenCallsSuccessfulCounter.increment()
        } catch (e: Exception) {
            tcTokenCallsWithErrorsCounter.increment()
            log.error("Failed to get tc token for identification session. useIdSessionId=$useIdSessionId", e)
            throw e
        }

        updateEIdSessionId(useIdSessionId, extractEIdSessionId(tcTokenWrapper.tcToken))

        return tcTokenWrapper.tcToken
    }

    fun getIdentity(
        eIdSessionId: UUID,
        identificationSession: IdentificationSession,
    ): GetResultResponse {
        val userData: GetResultResponse?
        try {
            userData = getSoapHandler().getResult(eIdSessionId.toString())
            getEidInformationCallsSuccessfulCounter.increment()
        } catch (e: Exception) {
            getEidInformationCallsWithErrorsCounter.increment()
            log.error("Failed to fetch identity data: ${e.message}.")
            throw e
        }

        // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
        if (userData.result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
            try {
                delete(identificationSession)
            } catch (e: Exception) {
                log.error("Failed to delete identification session. id=${identificationSession.id}")
            }
        } else {
            // resultMinor error codes can be found in TR 03130 Part 1 -> 3.4.1 Error Codes
            log.info("The resultMinor for identification session is ${userData.result.resultMinor}. id=${identificationSession.id}")
        }
        return userData
    }

    private fun getSoapHandler(): SoapHandler {
        if (soapHandler == null) {
            soapHandler = SoapHandler(soapConfiguration)
        }
        return soapHandler!!
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

    private fun updateEIdSessionId(useIdSessionId: UUID, eIdSessionId: UUID): IdentificationSession {
        val session = identificationSessionRepository.findByUseIdSessionId(useIdSessionId)
        session!!.eIdSessionId = eIdSessionId
        identificationSessionRepository.save(session)
        log.info("Updated eIdSessionId of identification session. useIdSessionId=${session.useIdSessionId}")
        return session
    }

    private fun delete(identificationSession: IdentificationSession) {
        identificationSessionRepository.delete(identificationSession)
        log.info("Deleted identification session. useIdSessionId=${identificationSession.useIdSessionId}")
    }
}
