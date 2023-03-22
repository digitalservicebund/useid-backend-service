package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.apikeys.ApiKeyDetails
import de.bund.digitalservice.useid.config.METRIC_NAME_EID_SERVICE_REQUESTS
import de.bund.digitalservice.useid.config.Tenant
import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.config.EidServiceConfiguration
import io.micrometer.core.annotation.Timed
import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.annotations.tags.Tag
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestAttribute
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val IDENTIFICATION_SESSIONS_BASE_PATH = "/api/v1/identification/sessions"
internal const val TCTOKEN_PATH_SUFFIX = "tc-token"

@RestController
@Timed
@RequestMapping(IDENTIFICATION_SESSIONS_BASE_PATH)
@Tag(
    name = "Identification Sessions",
    description = "An identification session represent an ongoing identification flow of a user and stores the required information.",
)
@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    name = "apiKey",
    `in` = SecuritySchemeIn.HEADER,
    paramName = "Authorization",
    scheme = "Bearer",
    description = "API key as bearer token in `Authorization` header",
)
class IdentificationSessionsController(
    private val identificationSessionService: IdentificationSessionService,
    private val eidServiceConfig: EidServiceConfiguration,
) {
    private val log = KotlinLogging.logger {}
    private val getEidInformationCallsSuccessfulCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "200")
    private val getEidInformationCallsWithErrorsCounter: Counter =
        Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", "get_eid_information", "status", "500")

    @PostMapping(produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Start session for a new identification as eService")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "401",
        description = "Authentication failed (missing or wrong api key)",
        content = [Content()],
    )
    @SecurityRequirement(name = "apiKey")
    fun startSession(
        authentication: Authentication,
        @RequestAttribute tenant: Tenant,
    ): ResponseEntity<CreateIdentificationSessionResponse> {
        val apiKeyDetails = authentication.details as ApiKeyDetails
        val tcTokenUrl =
            identificationSessionService.startSession(apiKeyDetails.refreshAddress!!, apiKeyDetails.requestDataGroups, tenant.id)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(CreateIdentificationSessionResponse(tcTokenUrl))
    }

    @GetMapping(
        path = ["/{useIdSessionId}/$TCTOKEN_PATH_SUFFIX"],
        produces = [MediaType.APPLICATION_XML_VALUE],
    )
    @Operation(summary = "Get TC token for this session")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "404",
        description = "No corresponding session found for that useIdSessionId",
        content = [Content()],
    )
    fun getTCToken(@PathVariable useIdSessionId: UUID): ResponseEntity<JakartaTCToken> {
        return try {
            val tcToken = identificationSessionService.startSessionWithEIdServer(useIdSessionId)
            ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_XML)
                .body(JakartaTCToken.fromTCTokenType(tcToken))
        } catch (e: IdentificationSessionNotFoundException) {
            log.error(e.message, e)
            ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        }
    }

    @GetMapping("/{eIdSessionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Fetch data as eService after identification was successful")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "404",
        description = "No corresponding session found for that eIdSessionId",
        content = [Content()],
    )
    @ApiResponse(
        responseCode = "401",
        description = "Authentication failed (missing or wrong api key)",
        content = [Content()],
    )
    @SecurityRequirement(name = "apiKey")
    fun getIdentity(
        @PathVariable eIdSessionId: UUID,
        @RequestAttribute tenantId: String,
        authentication: Authentication,
    ): ResponseEntity<GetResultResponseType> {
        val apiKeyDetails = authentication.details as ApiKeyDetails

        val userData: GetResultResponseType?
        val identificationSession = identificationSessionService.findByEIdSessionId(eIdSessionId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND).build()
        if (apiKeyDetails.refreshAddress != identificationSession.refreshAddress) {
            log.error("API key differs from the API key used to start the identification session.")
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build()
        }
        try {
            val eidService = EidService(eidServiceConfig)
            userData = eidService.getEidInformation(eIdSessionId.toString())
            createCounter("get_eid_information", "200", tenantId).increment()
        } catch (e: Exception) {
            createCounter("get_eid_information", "500", tenantId).increment()
            log.error("Failed to fetch identity data: ${e.message}.")
            throw e
        }

        // resultMajor for success can be found in TR 03112 Part 1 -> Section 4.1.2 ResponseType
        if (userData.result.resultMajor.equals("http://www.bsi.bund.de/ecard/api/1.1/resultmajor#ok")) {
            try {
                identificationSessionService.delete(identificationSession)
            } catch (e: Exception) {
                log.error("Failed to delete identification session. id=${identificationSession.id}")
            }
        } else {
            // resultMinor error codes can be found in TR 03130 Part 1 -> 3.4.1 Error Codes
            log.info("The resultMinor for identification session is ${userData.result.resultMinor}. id=${identificationSession.id}")
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(userData)
    }

    protected fun createCounter(method: String, status: String, tenantId: String = "unknown"): Counter {
        return Metrics.counter(METRIC_NAME_EID_SERVICE_REQUESTS, "method", method, "status", status, "tenant_id", tenantId)
    }
}
