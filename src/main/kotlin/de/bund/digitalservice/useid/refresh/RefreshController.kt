package de.bund.digitalservice.useid.refresh

import de.bund.digitalservice.useid.identification.IdentificationSessionNotFoundException
import de.bund.digitalservice.useid.identification.IdentificationSessionService
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.net.URI
import java.net.URLEncoder
import java.util.UUID
import java.util.stream.Collectors
import kotlin.text.Charsets.UTF_8

internal const val REFRESH_PATH = "/refresh"

@RestController
@Timed
@Tag(name = "Refresh", description = "The refresh endpoint redirects the caller to the refresh address of the respective service.")
@RequestMapping(REFRESH_PATH)
class RefreshController(private val identificationSessionService: IdentificationSessionService) {

    @GetMapping
    @Operation(summary = "Redirect user to eService (after identification)")
    @ApiResponse(responseCode = "303", content = [Content()])
    @ApiResponse(responseCode = "404", description = "There is no corresponding session found for the provided eIdSessionId", content = [Content()])
    fun redirectToEServiceRefreshAddress(
        @RequestParam("sessionId") eIdSessionId: UUID,
        @RequestParam requestQueryParams: Map<String, String>,
    ): ResponseEntity<Unit> {
        val session = identificationSessionService.findByEIdSessionId(eIdSessionId) ?: run {
            throw IdentificationSessionNotFoundException()
        }
        val responseQueryParams: String = buildEncodedQueryParameters(requestQueryParams)
        return ResponseEntity
            .status(HttpStatus.SEE_OTHER)
            .location(URI.create("${session.refreshAddress}?$responseQueryParams"))
            .build()
    }

    private fun buildEncodedQueryParameters(parameters: Map<String, String>): String =
        parameters.map { entry -> "${encode(entry.key)}=${encode(entry.value)}" }
            .stream().collect(Collectors.joining("&"))

    private fun encode(string: String): String = URLEncoder.encode(string, UTF_8)
}
