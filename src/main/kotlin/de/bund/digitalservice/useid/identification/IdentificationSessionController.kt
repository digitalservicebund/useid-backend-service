package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid240.GetResultResponse
import de.bund.digitalservice.useid.apikeys.ApiKeyDetails
import de.bund.digitalservice.useid.apikeys.InvalidApiKeyException
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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val IDENTIFICATION_SESSIONS_BASE_PATH = "/api/v1/identification/sessions"
internal const val TCTOKEN_PATH_SUFFIX = "tc-token"

@RestController
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
) {
    private val log = KotlinLogging.logger {}

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
    ): ResponseEntity<CreateIdentificationSessionResponse> {
        val apiKeyDetails = authentication.details as ApiKeyDetails
        val tcTokenUrl =
            identificationSessionService.startSession(apiKeyDetails.refreshAddress!!, apiKeyDetails.requestDataGroups)
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
        authentication: Authentication,
    ): ResponseEntity<GetResultResponse> {
        validateApiKey(authentication, eIdSessionId)

        val userData: GetResultResponse = identificationSessionService.getIdentity(eIdSessionId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(userData)
    }

    fun validateApiKey(authentication: Authentication, eIdSessionId: UUID) {
        val apiKeyDetails = authentication.details as ApiKeyDetails

        val identificationSession = identificationSessionService.findByEIdSessionId(eIdSessionId)
            ?: throw IdentificationSessionNotFoundException(eIdSessionId)
        if (apiKeyDetails.refreshAddress != identificationSession.refreshAddress) {
            throw InvalidApiKeyException("API key does not match with API key used to start the session.")
        }
    }
}
