package de.bund.digitalservice.useid.statics

import mu.KotlinLogging
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import reactor.core.publisher.Mono

@Controller
class WellKnownController {

    private val log = KotlinLogging.logger {}

    @GetMapping(
        path = [".well-known/apple-app-site-association"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun getAppleAppSiteAssociation(): Mono<ResponseEntity<IOSUniversalLink>> {
        val appIds = listOf(
            "VDTVKQ35RL.de.bund.digitalservice.UseID",
            "VDTVKQ35RL.de.bund.digitalservice.UseID-Preview"
        )
        val components = listOf(Component(pathUrlName = "/eID-Client"))
        val details = listOf(Details(appIds, components))
        val universalLink = UniversalLink(details)

        return Mono.just(
            ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(IOSUniversalLink(universalLink))
        )
            .doOnError {
                log.error("Failed to return iOS Universal Link config.", it)
            }
            .onErrorReturn(
                ResponseEntity.internalServerError().build()
            )
    }
}
