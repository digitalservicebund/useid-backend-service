package de.bund.digitalservice.useid.identification

import de.bund.digitalservice.useid.eidservice.EidAvailabilityCheck
import de.bund.digitalservice.useid.eidservice.EidAvailabilityRepository
import de.bund.digitalservice.useid.integration.RedisTestContainerConfig
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import java.util.Date

private const val E_SERVICE_HEALTH_PATH = "api/v1/health"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class EidAvailabilityControllerIntegrationTest(@Autowired val webTestClient: WebTestClient) : RedisTestContainerConfig() {

    @Autowired
    private lateinit var eidAvailabilityRepository: EidAvailabilityRepository

    @BeforeEach
    fun setup() {
        eidAvailabilityRepository.deleteAll()
    }

    @AfterAll
    fun teardown() {
        eidAvailabilityRepository.deleteAll()
    }

    @Test
    fun `should return 200 from health endpoint`() {
        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange().expectStatus().isOk
    }

    @Test
    fun `should return UP signal if eService has responded correctly for the last 5 minutes`() {
        var status = ""

        eidAvailabilityRepository.save(EidAvailabilityCheck("1", true, Date.from(Date().toInstant().minusSeconds(240))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("2", true, Date.from(Date().toInstant().minusSeconds(180))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("3", true, Date.from(Date().toInstant().minusSeconds(120))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("4", true, Date.from(Date().toInstant().minusSeconds(60))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("5", true, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status).isEqualTo("UP")
    }

    @Test
    fun `should return UP signal if eService has responded incorrectly for less than 75 percent in the last 5 minutes`() {
        var status = ""

        eidAvailabilityRepository.save(EidAvailabilityCheck("1", true, Date.from(Date().toInstant().minusSeconds(240))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("2", true, Date.from(Date().toInstant().minusSeconds(180))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("5", false, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status).isEqualTo("UP")
    }

    @Test
    fun `should return DOWN signal if eService has responded correctly only for the last minute`() {
        var status = ""

        eidAvailabilityRepository.save(EidAvailabilityCheck("1", false, Date.from(Date().toInstant().minusSeconds(240))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("2", false, Date.from(Date().toInstant().minusSeconds(180))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("5", true, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status).isEqualTo("DEGRADED")
    }

    @Test
    fun `should return DOWN signal if eService has not responded correctly for the last 5 minutes`() {
        var status = ""

        eidAvailabilityRepository.save(EidAvailabilityCheck("1", false, Date.from(Date().toInstant().minusSeconds(240))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("2", false, Date.from(Date().toInstant().minusSeconds(180))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("3", false, Date.from(Date().toInstant().minusSeconds(120))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("4", false, Date.from(Date().toInstant().minusSeconds(60))))
        eidAvailabilityRepository.save(EidAvailabilityCheck("5", false, Date()))

        webTestClient.get().uri(E_SERVICE_HEALTH_PATH).exchange()
            .expectStatus().isOk
            .expectHeader().contentType(MediaType.APPLICATION_JSON_VALUE)
            .expectBody().jsonPath("$.status").value<String> { status = it }
        assertThat(status).isEqualTo("DEGRADED")
    }
}
