package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.journey.JourneyTestApplicationProperties
import de.bund.digitalservice.useid.journey.JourneyTestConfig
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@Tag("journey")
@Import(JourneyTestConfig::class)
class ApplicationJourneyTest {

    @Autowired
    private lateinit var journeyTestApplicationProperties: JourneyTestApplicationProperties

    @Test
    fun `application health`() {
        WebTestClient.bindToServer()
            .baseUrl(journeyTestApplicationProperties.staging!!.url)
            .build()
            .get()
            .uri("/actuator/health")
            .exchange()
            .expectStatus()
            .isOk
    }
}
