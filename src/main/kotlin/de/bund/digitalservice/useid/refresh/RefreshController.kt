package de.bund.digitalservice.useid.refresh

import de.bund.digitalservice.useid.identification.IdentificationSessionService
import io.micrometer.core.annotation.Timed
import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.net.URI
import java.util.UUID
import java.util.stream.Collectors

internal const val REFRESH_PATH = "/refresh"

@RestController
@Timed
@RequestMapping(REFRESH_PATH)
class RefreshController(private val identificationSessionService: IdentificationSessionService) {

    private val log = KotlinLogging.logger {}

    @GetMapping
    fun redirectToEServiceRefreshAddress(@RequestParam("sessionId") eIDSessionId: UUID, @RequestParam allParams: Map<String, String>): Mono<ResponseEntity<Unit>> {
        return identificationSessionService.findByEIDSessionId(eIDSessionId)
            .doOnError {
                log.error("Failed to load identification session.", it)
            }
            .map {
                val requestParams = allParams.map { entry -> "${entry.key}=${entry.value}" }.stream().collect(Collectors.joining("&"))
                ResponseEntity
                    .status(HttpStatus.SEE_OTHER)
                    .location(URI.create("${it.refreshAddress}?$requestParams"))
                    .build<Unit>()
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
            .onErrorReturn(
                ResponseEntity.internalServerError().build()
            )
    }
}
