package de.bund.digitalservice.useid.timebasedtokens

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.documentation.EIDClientTag
import de.bund.digitalservice.useid.documentation.WidgetTag
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

internal const val TIME_BASED_TOKENS_BASE_PATH = "${ApplicationProperties.apiVersionPrefix}/sessions"
internal const val TIME_BASED_TOKENS_SUB_PATH = "time-based-tokens"

@RestController
@Timed
@RequestMapping(TIME_BASED_TOKENS_BASE_PATH)
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TimeBasedTokenController(private val timeBasedTokenService: TimeBasedTokenService) {
    @PostMapping("/{sessionId}/$TIME_BASED_TOKENS_SUB_PATH", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create time-based token for this session")
    @WidgetTag
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400", description = "Invalid sessionId (session not found)", content = [Content()])
    fun getTimeBasedToken(@PathVariable sessionId: UUID): ResponseEntity<TimeBasedToken> {
        val token: TimeBasedToken
        try {
            token = timeBasedTokenService.updateOrCreate(sessionId)
        } catch (e: InvalidSessionIdException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null)
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(token)
    }

    @GetMapping("/{sessionId}/$TIME_BASED_TOKENS_SUB_PATH/{tokenId}")
    @Operation(summary = "Check if time-based token for this session is (still) valid")
    @EIDClientTag
    @ApiResponse(responseCode = "204", description = "Token was found and is valid", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Token was not found (or not valid anymore)", content = [Content()])
    fun timeBasedTokenValidation(@PathVariable sessionId: UUID, @PathVariable tokenId: UUID): ResponseEntity<Nothing> {
        val isTokenValid = timeBasedTokenService.isTokenValid(sessionId, tokenId)
        return ResponseEntity
            .status(if (isTokenValid) HttpStatus.NO_CONTENT else HttpStatus.NOT_FOUND)
            .body(null)
    }
}
