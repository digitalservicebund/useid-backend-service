package de.bund.digitalservice.useid.tracking

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.net.URI

@Service
class WebRequests(private val client: WebClient) {

    fun POST(url: String): Mono<ResponseEntity<Void>> {
        return client
            .post()
            .uri(URI(url))
            .retrieve()
            .toBodilessEntity()
            .onErrorReturn(
                // everything but 200 will be caught and responded with 500
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null)
            )
    }
}
