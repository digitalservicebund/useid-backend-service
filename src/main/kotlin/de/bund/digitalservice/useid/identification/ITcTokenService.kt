package de.bund.digitalservice.useid.identification

import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import reactor.core.publisher.Mono

interface ITcTokenService {
    fun getTcToken(refreshAddress: String): Mono<TCTokenType>
}
