package de.bund.digitalservice.useid.refresh

import de.bund.digitalservice.useid.identification.IdentificationSessionService
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

@RestController
@RequestMapping("/refresh")
class RefreshController(private val identificationSessionService: IdentificationSessionService) {

    private val log = KotlinLogging.logger {}

    @GetMapping
    fun redirectService(@RequestParam sessionId: UUID): Mono<ResponseEntity<Unit>> {
        return identificationSessionService.findByEIDSessionId(sessionId)
            .doOnError { exception ->
                log.error { "error occurred while checking $sessionId;\n ${exception.message}" }
            }
            .map {
                ResponseEntity
                    .status(HttpStatus.SEE_OTHER)
                    .location(URI.create(it.refreshAddress))
                    .build<Unit>()
            }
            .defaultIfEmpty(ResponseEntity.status(HttpStatus.NOT_FOUND).build())
            .onErrorReturn(
                ResponseEntity.internalServerError().build()
            )
    }
}
