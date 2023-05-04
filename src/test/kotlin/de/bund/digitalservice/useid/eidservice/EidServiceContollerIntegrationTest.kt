package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidServiceHealthDataPoint
import de.bund.digitalservice.useid.eidservice.EidServiceRepository
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Date

private const val E_SERVICE_HEALTH_PATH = "api/v1/eidservice/health"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class EidServiceContollerIntegrationTest(@Autowired val webTestClient: WebTestClient) {

    @Autowired
    private lateinit var eidServiceRepository: EidServiceRepository

    @Test
    fun `should return 200 from health endpoint if called without authentication`() {
        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange().expectStatus().isOk
    }

    @Test
    fun `should return UP signal if eService responds responds correctly`() {
        val expectedStatus = "UP"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", true, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(expectedStatus, equalTo(status))
    }

    @Test
    fun `should return DOWN signal if eService does not respond correctly`() {
        val expectedStatus = "DOWN"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", false, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(expectedStatus, equalTo(status))
    }
}
