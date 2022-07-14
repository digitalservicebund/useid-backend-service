package de.bund.digitalservice.useid

import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class QRCodeImageControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired @Value("\${local.server.port}")
    val port: Int
) {
    private val fixture = ClassPathResource("qr-300-digitalservice-bund-de.png")
    private val fixtureByteArray = fixture.file.readBytes()

    @Test
    fun `should return correct QR Code when the url is not encoded`() {
        // WebTestClient's uri() when given a string will use Spring's default uri builder which encodes the given string,
        // thus using `URI.create()` to make explicit the url being requested!
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/v1/qrcode/300?url=https://digitalservice.bund.de/"))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.IMAGE_PNG_VALUE)
            .expectBody<ByteArray>()
            .isEqualTo(fixtureByteArray)
    }

    @Test
    fun `should return correct QR Code when the url is encoded`() {
        // WebTestClient's uri() when given a string will use Spring's default uri builder which encodes the given string,
        // thus using `URI.create()` to make explicit the url being requested!
        webTestClient
            .get()
            .uri(URI.create("http://localhost:$port/v1/qrcode/300?url=https%3A%2F%2Fdigitalservice.bund.de%2F"))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.IMAGE_PNG_VALUE)
            .expectBody<ByteArray>()
            .isEqualTo(fixtureByteArray)
    }
}
