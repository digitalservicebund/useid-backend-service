package de.bund.digitalservice.useid.identification

import de.bund.bsi.eid230.GetResultResponseType
import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.documentation.EServiceTag
import de.bund.digitalservice.useid.tenant.InvalidTenantException
import de.bund.digitalservice.useid.tenant.Tenant
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val IDENTIFICATIONS_BASE_PATH = "${ApplicationProperties.apiVersionPrefix}/identifications"

@RestController
@Timed
@SecurityScheme(
    type = SecuritySchemeType.HTTP,
    name = "apiKey",
    `in` = SecuritySchemeIn.HEADER,
    paramName = "Authorization",
    scheme = "Bearer",
    description = "API key as bearer token in `Authorization` header",
)
@EServiceTag
class IdentificationSessionsController(private val identificationSessionService: IdentificationSessionService) {
    @PostMapping(IDENTIFICATIONS_BASE_PATH, produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Start session for a new identification as eService")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "403",
        description = "Authentication failed (missing or wrong api key)",
        content = [Content()],
    )
    @SecurityRequirement(name = "apiKey")
    fun startSession(
        authentication: Authentication,
    ): ResponseEntity<CreateIdentificationSessionResponse> {
        val tenant = authentication.details as Tenant
        val tcTokenUrl =
            identificationSessionService.startSession(tenant.refreshAddress, tenant.dataGroups, tenant.id)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(CreateIdentificationSessionResponse(tcTokenUrl))
    }

    @GetMapping("$IDENTIFICATIONS_BASE_PATH/{eIdSessionId}", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Fetch data as eService after identification was successful")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "404",
        description = "No corresponding session found for that eIdSessionId",
        content = [Content()],
    )
    @ApiResponse(
        responseCode = "403",
        description = "Authentication failed (missing or wrong api key)",
        content = [Content()],
    )
    @SecurityRequirement(name = "apiKey")
    fun getIdentity(
        @PathVariable eIdSessionId: UUID,
        authentication: Authentication,
    ): ResponseEntity<GetResultResponseType> {
        val tenant = authentication.details as Tenant
        validateTenant(tenant, eIdSessionId)

        val identity = identificationSessionService.getIdentity(eIdSessionId, tenant.id)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(identity)
    }

    fun validateTenant(tenant: Tenant, eIdSessionId: UUID) {
        val identificationSession = identificationSessionService.findByEIdSessionIdOrThrow(eIdSessionId)
        if (tenant.id != identificationSession.tenantId) {
            throw InvalidTenantException("Tenant does not match with tenant used to start the session.")
        }
    }
}
