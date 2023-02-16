package de.bund.digitalservice.useid.tracking

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.net.http.HttpResponse.BodyHandlers

@Service
class WebRequests(private val client: HttpClient) {

    fun POST(url: String): Boolean {
        val request: HttpRequest = HttpRequest.newBuilder()
            .uri(URI(url))
            .POST(HttpRequest.BodyPublishers.noBody())
            .build()

        val response: HttpResponse<String>
        try {
            response = client.send(request, BodyHandlers.ofString())
        } catch (e: Exception) {
            return false
        }

        return response.statusCode() == HttpStatus.OK.value()
    }
}
