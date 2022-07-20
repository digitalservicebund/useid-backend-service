package de.bund.digitalservice.useid.identification

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TcTokenUrlService {
    fun getTcTokenUrl(): Mono<String> {
        // Currently mock tcTokenUrl
        val mockTcTokenDataSource = "https://useid.dev/12345678"
        return Mono.just(mockTcTokenDataSource)
    }
}
