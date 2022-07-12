package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.model.IdentityAttributes
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class IdentityService() {
    fun getIdentity(sessionId: String): Mono<IdentityAttributes> {
        // Currently returns a mock response
        return Mono.just(
            IdentityAttributes("firstname", "lastname")
        )
    }
}
