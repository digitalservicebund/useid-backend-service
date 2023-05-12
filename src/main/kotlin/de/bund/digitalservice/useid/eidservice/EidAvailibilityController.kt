package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.config.ApplicationProperties
import de.bund.digitalservice.useid.documentation.MetricTag
import io.micrometer.core.annotation.Timed
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@Timed
class EidAvailibilityController(private val eidAvailabilityService: EidAvailabilityService) {

    @GetMapping("${ApplicationProperties.apiVersionPrefix}/health", produces = [MediaType.APPLICATION_JSON_VALUE])
    @Operation(summary = "Check the health of the eId-Server")
    @ApiResponse(responseCode = "200")
    @MetricTag
    fun getEidAvailability(): ResponseEntity<String> {
        if (!eidAvailabilityService.checkFunctionalityOfEidService()) {
            return ResponseEntity
                .status(HttpStatus.OK)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"status\":\"DEGRADED\",\"groups\":[\"eService\"]}")
        }
        return ResponseEntity
            .status(HttpStatus.OK)
            .contentType(MediaType.APPLICATION_JSON)
            .body("{\"status\":\"UP\",\"groups\":[\"eService\"]}")
    }
}
