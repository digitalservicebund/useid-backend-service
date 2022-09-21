package de.bund.digitalservice.useid

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment
import org.springframework.test.web.reactive.server.WebTestClient
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Tag("integration")
@Testcontainers
class HealthEndpointIntegrationTest(@Autowired val webTestClient: WebTestClient) {
    @Container
    private val postgresqlContainer = PostgreSQLContainer("postgres:12")

    @Test
    fun `should expose health endpoint`() {
        webTestClient.get().uri("/actuator/health").exchange().expectStatus().isOk
    }
}
