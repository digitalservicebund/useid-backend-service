package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.integration.RedisTestContainerConfig
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class HealthEndpointIntegrationTest(@Autowired val webTestClient: WebTestClient) : RedisTestContainerConfig() {
    @Test
    fun `should expose health endpoint`() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk
    }
}
