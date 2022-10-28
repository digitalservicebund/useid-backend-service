package de.bund.digitalservice.useid.tracking

import mu.KotlinLogging
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Service
class WebRequests {

    private val log = KotlinLogging.logger {}
    fun POST(url: String): Mono<HttpStatus> {
        val client = WebClient.create()

        return client.post()
            .uri(URI(url))
            .retrieve()
            .toBodilessEntity()
            .map {
                if (it.statusCode != HttpStatus.OK) {
                    log.error { "post request failed: $it" }
                }
                it.statusCode
            }
    }
}
