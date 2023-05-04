package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.config.ApplicationProperties
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import kotlin.jvm.optionals.getOrNull

@RestController
@Timed
class EidServiceController(private val eidServiceRepository: EidServiceRepository) {

    @GetMapping("${ApplicationProperties.apiVersionPrefix}/eidservice/health", produces = [MediaType.APPLICATION_JSON_VALUE])
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
    fun getEidServiceHealth(): ResponseEntity<String> {
        if (!checkFunctionalityOfEidService()) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\":\"DOWN\",\"groups\":[\"eService\"]}")
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"status\":\"UP\",\"groups\":[\"eService\"]}")
    }

    fun checkFunctionalityOfEidService(): Boolean {
        val lastResult = eidServiceRepository.findById("1").getOrNull() ?: return false
        return lastResult.up
    }
}
