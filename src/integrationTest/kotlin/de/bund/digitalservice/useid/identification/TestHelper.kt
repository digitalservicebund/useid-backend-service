package de.bund.digitalservice.useid.identification

import de.governikus.panstar.sdk.soap.handler.SoapHandler
import de.governikus.panstar.sdk.soap.handler.TcTokenWrapper
import de.governikus.panstar.sdk.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import org.springframework.http.HttpHeaders
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.util.UUID

private const val AUTHORIZATION_HEADER = "Bearer valid-api-key-1"

internal fun WebTestClient.sendStartSessionRequest() =
    this.post()
        .uri(TEST_IDENTIFICATIONS_BASE_PATH)
        .headers { setAuthorizationHeader(it) }
        .exchange()

internal fun WebTestClient.sendGETRequest(uri: String) =
    this.get()
        .uri(uri)

internal fun IdentificationSessionRepository.retrieveIdentificationSession(useIdSessionId: UUID) =
    this.findByUseIdSessionId(useIdSessionId)

internal fun mockTcToken(soapHandler: SoapHandler, refreshAddress: String) {
    val mockTCToken = mockk<TCTokenType>(relaxed = true)
    every { mockTCToken.refreshAddress } returns refreshAddress
    val mockTCTokenWrapper = mockk<TcTokenWrapper>(relaxed = true)
    every { mockTCTokenWrapper.tcToken } returns mockTCToken
    every { soapHandler.getTcToken(any(), any()) } returns mockTCTokenWrapper
}

internal fun extractRelativePathFromURL(url: String) = URI.create(url).rawPath

internal fun extractUseIdSessionIdFromTcTokenUrl(tcTokenURL: String): UUID {
    val pathSegments = UriComponentsBuilder
        .fromHttpUrl(tcTokenURL)
        .encode().build().pathSegments
    val useIdSessionId = pathSegments[pathSegments.size - 1]
    return UUID.fromString(useIdSessionId)!!
}

internal fun setAuthorizationHeader(headers: HttpHeaders) {
    headers.set(HttpHeaders.AUTHORIZATION, AUTHORIZATION_HEADER)
}
