package de.bund.digitalservice.useid.identification

import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
@ConditionalOnProperty("feature.eid-service-integration.enabled", havingValue = "false")
class NoopTcTokenService : ITcTokenService {
    override fun getTcToken(refreshAddress: String): Mono<TCTokenType> {
        return Mono.empty()
    }
}
