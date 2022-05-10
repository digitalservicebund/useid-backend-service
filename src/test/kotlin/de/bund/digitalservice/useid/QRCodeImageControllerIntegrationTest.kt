package de.bund.digitalservice.useid

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class QRCodeImageControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Test
    fun `should return correct QR Code when the url is encoded`() {
        val fixture = ClassPathResource("qr-300-digitalservice-bund-de.png")
        val fixtureByteArray = fixture.file.readBytes()
        webTestClient
            .get()
            .uri("/api/v1/qrcode/300?url=https%3A%2F%2Fdigitalservice.bund.de%2F")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.IMAGE_PNG_VALUE)
            .expectBody<ByteArray>()
            .isEqualTo(fixtureByteArray)
    }

    @Test
    fun `should return correct QR Code when the url is not encoded`() {
        val fixture = ClassPathResource("qr-300-digitalservice-bund-de.png")
        val fixtureByteArray = fixture.file.readBytes()
        webTestClient
            .get()
            .uri("/api/v1/qrcode/300?url=https://digitalservice.bund.de/")
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.IMAGE_PNG_VALUE)
            .expectBody<ByteArray>()
            .isEqualTo(fixtureByteArray)
    }
}
