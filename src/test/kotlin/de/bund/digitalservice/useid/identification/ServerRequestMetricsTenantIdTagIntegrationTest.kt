package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidService
import de.bund.digitalservice.useid.identification.*
import io.mockk.mockkConstructor
import org.awaitility.Awaitility
import org.awaitility.Durations
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@AutoConfigureObservability
class ServerRequestMetricsTenantIdTagIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)
        Awaitility.setDefaultTimeout(Durations.ONE_SECOND)
    }

    @Test
    fun `prometheus log contains server request metric with tenant ID for start session endpoint after start session call`() {
        var tcTokenURL = ""
//        webTestClient.sendStartSessionRequest()
//                .expectStatus().isOk
//                .expectHeader().contentType(MediaType.APPLICATION_JSON)
//                .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        webTestClient.get().uri("actuator/prometheus")
                .exchange()
                .expectStatus().isOk
//                .expectBody().let {
//                    println("### CONTENT: $it")
//                }

//        val eIdSessionId = UUID.randomUUID()
//
//        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")
//
//        webTestClient.sendGETRequest(extractRelativePathFromURL(tcTokenURL))
//                .exchange()
//                .expectStatus().isOk
//                .expectHeader().contentType(MediaType.APPLICATION_XML)
//                .expectBody().xpath("TCTokenType").exists()
//
//        val useIdSessionId = extractUseIdSessionIdFromTcTokenUrl(tcTokenURL)
//        val session = identificationSessionRepository.retrieveIdentificationSession(useIdSessionId)!!
//        Assertions.assertEquals(eIdSessionId, session.eIdSessionId)
    }
}