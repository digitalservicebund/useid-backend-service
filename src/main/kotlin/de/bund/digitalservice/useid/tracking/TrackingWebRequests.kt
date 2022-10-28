package de.bund.digitalservice.useid.tracking

import mu.KotlinLogging
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Service
class TrackingWebRequests {
    private val log = KotlinLogging.logger {}
    private val httpClient: HttpClient = HttpClient.newBuilder().build()
    private val responseHandler: HttpResponse.BodyHandler<String> = HttpResponse.BodyHandlers.ofString()

    fun GET(url: String) {
        val request = HttpRequest.newBuilder()
            .uri(URI.create(url))
            .build()

        val response = httpClient.send(request, responseHandler)
        val status = response.statusCode()
        if (status == 200) {
            log.info("$status, successfully tracked: $url")
        } else {
            log.error("$status, tracking failed for: $url")
        }
    }
}
