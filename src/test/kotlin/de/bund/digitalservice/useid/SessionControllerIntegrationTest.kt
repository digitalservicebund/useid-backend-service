package de.bund.digitalservice.useid

import de.bund.digitalservice.useid.model.ClientRequestSession
import de.bund.digitalservice.useid.utils.IdGenerator
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.reactive.function.BodyInserters
import java.net.URI

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class SessionControllerIntegrationTest(
    @Autowired val webTestClient: WebTestClient,
    @Autowired @Value("\${local.server.port}") val port: Int
) {
    val attributes = listOf("DG1", "DG2")

    @Test
    fun `should return TCToken Url and Session Id when request is made with correct payload`() {
        val uuid = "my-fake-uuid"
        mockkObject(IdGenerator)
        every { IdGenerator.generateUUID() } returns uuid

        webTestClient
            .post()
            .uri(URI.create("http://localhost:$port/api/v1/session"))
            .body(BodyInserters.fromValue(ClientRequestSession("https://digitalservice.bund.de", attributes)))
            .exchange()
            .expectStatus()
            .isOk
            .expectHeader()
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody()
            .jsonPath("$.tcTokenUrl").isEqualTo("https://digitalservice.bund.de")
            .jsonPath("$.sessionId").isEqualTo("my-fake-uuid")

        unmockkObject(IdGenerator)
    }
}
