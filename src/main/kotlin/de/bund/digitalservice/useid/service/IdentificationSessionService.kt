package de.bund.digitalservice.useid.service

import de.bund.digitalservice.useid.model.CreateIdentitySessionRequest
import de.bund.digitalservice.useid.model.CreateIdentitySessionResponse
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.UUID

@Service
class IdentificationSessionService() {
    fun createSession(createIdentitySessionRequest: CreateIdentitySessionRequest): Mono<CreateIdentitySessionResponse> {
        return Mono.just(
            CreateIdentitySessionResponse(createIdentitySessionRequest.refreshAddress, UUID.randomUUID().toString())
        )
    }
}
