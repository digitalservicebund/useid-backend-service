package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidServiceHealthDataPoint
import de.bund.digitalservice.useid.eidservice.EidServiceRepository
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.BeforeEach
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

    @BeforeEach
    fun setup() {
        eidServiceRepository.deleteAll()
    }

    @Test
    fun `should return 200 from health endpoint if called without authentication`() {
        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange().expectStatus().isOk
    }

    @Test
    fun `should return UP signal if eService has responded correctly for the last 5 minutes`() {
        val expectedStatus = "UP"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", true, Date.from(Date().toInstant().minusSeconds(240))))
        eidServiceRepository.save(EidServiceHealthDataPoint("2", true, Date.from(Date().toInstant().minusSeconds(180))))
        eidServiceRepository.save(EidServiceHealthDataPoint("3", true, Date.from(Date().toInstant().minusSeconds(120))))
        eidServiceRepository.save(EidServiceHealthDataPoint("4", true, Date.from(Date().toInstant().minusSeconds(60))))
        eidServiceRepository.save(EidServiceHealthDataPoint("5", true, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status, equalTo(expectedStatus))
    }

    @Test
    fun `should return UP signal if eService has responded incorrectly for less than 75 percent in the last 5 minutes`() {
        val expectedStatus = "UP"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", true, Date.from(Date().toInstant().minusSeconds(240))))
        eidServiceRepository.save(EidServiceHealthDataPoint("2", true, Date.from(Date().toInstant().minusSeconds(180))))
        eidServiceRepository.save(EidServiceHealthDataPoint("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidServiceRepository.save(EidServiceHealthDataPoint("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidServiceRepository.save(EidServiceHealthDataPoint("5", false, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status, equalTo(expectedStatus))
    }

    @Test
    fun `should return DOWN signal if eService has responded correctly only for the last minute`() {
        val expectedStatus = "DOWN"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", false, Date.from(Date().toInstant().minusSeconds(240))))
        eidServiceRepository.save(EidServiceHealthDataPoint("2", false, Date.from(Date().toInstant().minusSeconds(180))))
        eidServiceRepository.save(EidServiceHealthDataPoint("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidServiceRepository.save(EidServiceHealthDataPoint("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidServiceRepository.save(EidServiceHealthDataPoint("5", true, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status, equalTo(expectedStatus))
    }

    @Test
    fun `should return DOWN signal if eService has not responded correctly for the last 5 minutes`() {
        val expectedStatus = "DOWN"
        var status = ""

        eidServiceRepository.save(EidServiceHealthDataPoint("1", false, Date.from(Date().toInstant().minusSeconds(240))))
        eidServiceRepository.save(EidServiceHealthDataPoint("2", false, Date.from(Date().toInstant().minusSeconds(180))))
        eidServiceRepository.save(EidServiceHealthDataPoint("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidServiceRepository.save(EidServiceHealthDataPoint("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidServiceRepository.save(EidServiceHealthDataPoint("5", false, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status, equalTo(expectedStatus))
    }
}
