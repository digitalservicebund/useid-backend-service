package de.bund.digitalservice.useid.tracking

import org.springframework.stereotype.Service

@Service
class WebRequests() {

    fun POST(url: String): Boolean {
       /* val response = client
            .post()
            .uri(URI(url))
            .retrieve()
            .toBodilessEntity()
            .onErrorReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null))
            .block()*/
        return false // response?.statusCode == HttpStatus.OK
    }
}
