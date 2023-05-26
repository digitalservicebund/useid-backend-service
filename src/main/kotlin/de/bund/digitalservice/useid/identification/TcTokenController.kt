package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.documentation.EIDClientTag
import de.bund.digitalservice.useid.tenant.REQUEST_ATTR_TENANT
import de.bund.digitalservice.useid.tenant.TenantProperties
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.*

internal const val TCTOKENS_BASE_PATH = "${ApplicationProperties.apiVersionPrefix}/tc-tokens"

@RestController
@Timed
@EIDClientTag
@RequestMapping(TCTOKENS_BASE_PATH)
class TcTokenController(private val identificationSessionService: IdentificationSessionService, private val tenantProperties: TenantProperties) {

    @GetMapping(
        path = ["/{useIdSessionId}"],
        produces = [MediaType.APPLICATION_XML_VALUE],
    )
    @Operation(summary = "Get TC token for this session")
    @ApiResponse(responseCode = "200")
    @ApiResponse(
        responseCode = "404",
        description = "No corresponding session found for that useIdSessionId",
        content = [Content()],
    )
    fun getTCToken(@PathVariable useIdSessionId: UUID, request: HttpServletRequest): ResponseEntity<JakartaTCToken> {
        identificationSessionService.findByUseIdSessionIdOrThrow(useIdSessionId).let { identificationSession ->
            identificationSession.tenantId?.let { tenantId ->
                tenantProperties.findByTenantId(tenantId).let { tenant ->
                    request.setAttribute(REQUEST_ATTR_TENANT, tenant)
                }
            }
        }

        val tcToken = identificationSessionService.startSessionWithEIdServer(useIdSessionId)
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_XML)
            .body(JakartaTCToken.fromTCTokenType(tcToken))
    }
}
