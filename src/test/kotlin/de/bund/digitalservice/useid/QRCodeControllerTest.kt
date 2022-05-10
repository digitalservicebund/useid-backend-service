package de.bund.digitalservice.useid

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@ExtendWith(SpringExtension::class)
@TestPropertySource(locations = ["classpath:application.properties"])
internal class QRCodeControllerTest {
    @Value("\${application.staging.url}")
    private val stagingUrl: String? = null

    @Test
    fun `should disable X-Frame-Options`() {
        WebTestClient.bindToServer()
            .baseUrl(stagingUrl!!)
            .build()
            .get()
            .uri("/widget")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .doesNotExist("X-Frame-Options")
    }
}
