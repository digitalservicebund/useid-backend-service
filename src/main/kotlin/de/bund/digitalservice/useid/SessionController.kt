package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.model.ClientResponseTCTokenUrl
import de.bund.digitalservice.useid.service.TCTokenUrlService
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import javax.validation.Valid

@RestController
@RequestMapping("/api/v1")
class SessionController(private val tcTokenUrlService: TCTokenUrlService) {
    @PostMapping(
        path = ["/init-session"],
        produces = [MediaType.APPLICATION_JSON_VALUE]
    )
    fun initSession(@Valid @RequestBody clientRequestSession: ClientRequestSession): Mono<ClientResponseTCTokenUrl> {
        return tcTokenUrlService.getTCTokenUrl(clientRequestSession)
    }
}
