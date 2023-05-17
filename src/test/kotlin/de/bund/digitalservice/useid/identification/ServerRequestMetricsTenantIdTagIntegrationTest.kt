package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidService
import io.mockk.mockkConstructor
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.TestPropertySource
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
@AutoConfigureObservability
@TestPropertySource(properties = ["management.endpoints.web.exposure.include=prometheus"])
class ServerRequestMetricsTenantIdTagIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @BeforeAll
    fun setup() {
        mockkConstructor(EidService::class)
    }

    @Test
    fun `prometheus log contains server request metric with tenant ID for start session endpoint after start session call`() {
        val expectedTenantId = "integration_test_1"
        val expectedPrometheusLogRegex = Regex("http_server_requests_seconds_count\\{error=\"none\",exception=\"none\",method=\"POST\",outcome=\"SUCCESS\",status=\"200\",tenant_id=\"(.+)\",uri=\"/api/v1/identifications\",}")

        webTestClient.sendStartSessionRequest()

        webTestClient.createGETRequest("actuator/prometheus")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody?.let { prometheusLogRaw ->
            val prometheusLog = String(bytes = prometheusLogRaw)
            val result = expectedPrometheusLogRegex.find(prometheusLog)
            val tenantId = result?.groupValues?.get(1)

            Assertions.assertEquals(expectedTenantId, tenantId)
        }
    }

    @Test
    fun `prometheus log contains server request metric with tenant ID for tc token endpoint after tc token is requested`() {
        var tcTokenURL = ""
        val expectedTenantId = "integration_test_1"
        val expectedPrometheusLogRegex = Regex("http_server_requests_seconds_count\\{error=\"none\",exception=\"none\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",tenant_id=\"(.+)\",uri=\"/api/v1/tc-tokens/\\{useIdSessionId}\",}")

        webTestClient.sendStartSessionRequest()
            .expectBody().jsonPath("$.tcTokenUrl").value<String> { tcTokenURL = it }

        val eIdSessionId = UUID.randomUUID()

        mockTcToken("https://www.foobar.com?sessionId=$eIdSessionId")

        webTestClient.createGETRequest(extractRelativePathFromURL(tcTokenURL))
            .exchange()

        webTestClient.get().uri("actuator/prometheus")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody?.let { prometheusLogRaw ->
            val prometheusLog = String(bytes = prometheusLogRaw)
            val result = expectedPrometheusLogRegex.find(prometheusLog)
            val tenantId = result?.groupValues?.get(1)

            Assertions.assertEquals(expectedTenantId, tenantId)
        }
    }

    @Test
    fun `prometheus log contains server request metric with tenant ID for widget endpoint after widget is requested`() {
        val expectedTenantId = "integration_test_1"
        val expectedPrometheusLogRegex = Regex("http_server_requests_seconds_count\\{error=\"none\",exception=\"none\",method=\"GET\",outcome=\"SUCCESS\",status=\"200\",tenant_id=\"(.+)\",uri=\"/widget\",}")
        val allowedHost = "i.am.allowed.1"

        webTestClient.createGETRequest("/widget?hostname=$allowedHost")
            .exchange()
            .expectStatus().isOk

        webTestClient.get().uri("actuator/prometheus")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .returnResult()
            .responseBody?.let { prometheusLogRaw ->
            val prometheusLog = String(bytes = prometheusLogRaw)
            val result = expectedPrometheusLogRegex.find(prometheusLog)
            val tenantId = result?.groupValues?.get(1)

            Assertions.assertEquals(expectedTenantId, tenantId)
        }
    }
}
