package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidService
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import mu.KotlinLogging
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class TcTokenService(
    private val eidService: EidService
) {
    private val log = KotlinLogging.logger {}
    fun getTcToken(refreshAddress: String): Mono<TCTokenType> {
        /* TODO: current implementation is a blocking operation
        * We cannot just wrap a blocking call with a Mono or a Flux
        * https://betterprogramming.pub/how-to-avoid-blocking-in-reactive-java-757ec7024676
        * and also usage of Reactor's BlockHound is preferred
        */
        return Mono.just(eidService.getTcToken(refreshAddress))
    }
}
