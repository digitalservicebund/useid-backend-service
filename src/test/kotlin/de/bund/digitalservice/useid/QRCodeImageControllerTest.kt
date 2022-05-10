package de.bund.digitalservice.useid

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.io.ClassPathResource
import org.springframework.http.MediaType
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody

@ExtendWith(SpringExtension::class)
@TestPropertySource(locations = ["classpath:application.properties"])
internal class QRCodeImageControllerTest {
    @Value("\${application.staging.url}")
    private val stagingUrl: String? = null
    private val fixture = ClassPathResource("qr-300-digitalservice-bund-de.png")
    private val fixtureByteArray = fixture.file.readBytes()

    @Test
    fun `should return correct QR Code when the url is encoded`() {
        WebTestClient.bindToServer()
            .baseUrl(stagingUrl!!)
            .build()
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
        WebTestClient.bindToServer()
            .baseUrl(stagingUrl!!)
            .build()
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
