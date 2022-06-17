package de.bund.digitalservice.useid

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@Tag("journey")
@TestPropertySource(locations = ["classpath:application.yaml"])
class ApplicationJourneyTest {
    @Value("\${application.staging.url}")
    private val stagingUrl: String? = null

    @Test
    fun `application health`() {
        WebTestClient.bindToServer()
            .baseUrl(stagingUrl!!)
            .build()
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk
    }
}
