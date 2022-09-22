package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.reactive.server.WebTestClient

class HealthEndpointIntegrationTest(@Autowired val webTestClient: WebTestClient) : PostgresTestcontainerIntegrationTest() {
    @Test
    fun `should expose health endpoint`() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk
    }
}
