package de.bund.digitalservice.useid.timebasedtokens

import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
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

@RestController
@Timed
@RequestMapping(IDENTIFICATION_SESSIONS_BASE_PATH)
@Tag(name = "Time-based Tokens", description = "Time-based tokens inside the widget that are only 60 seconds valid and can be checked by the app.")
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class TimeBasedTokenController(private val timeBasedTokenService: TimeBasedTokenService) {
    @PostMapping("/{useIdSessionId}/tokens", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Create time-based token for this session")
    @ApiResponse(responseCode = "200")
    @ApiResponse(responseCode = "400", description = "Invalid useIdSessionId (session not found)", content = [Content()])
    fun getTimeBasedToken(@PathVariable useIdSessionId: UUID): ResponseEntity<TimeBasedToken> {
        val token: TimeBasedToken
        try {
            token = timeBasedTokenService.updateOrCreate(useIdSessionId)
        } catch (e: InvalidUseIdSessionIdException) {
            return ResponseEntity.status(400).body(null)
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body(token)
    }

    @GetMapping("/{useIdSessionId}/tokens/{tokenId}")
    @Operation(summary = "Check if time-based token for this session is (still) valid")
    @ApiResponse(responseCode = "204", description = "Token was found and is valid", content = [Content()])
    @ApiResponse(responseCode = "404", description = "Token was not found (or not valid anymore)", content = [Content()])
    fun timeBasedTokenValidation(@PathVariable useIdSessionId: UUID, @PathVariable tokenId: UUID): ResponseEntity<Nothing> {
        val isTokenValid = timeBasedTokenService.isTokenValid(useIdSessionId, tokenId)
        return ResponseEntity
            .status(if (isTokenValid) HttpStatus.NO_CONTENT else HttpStatus.NOT_FOUND)
            .body(null)
    }
}
