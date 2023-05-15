package de.bund.digitalservice.useid.eidservice

import de.bund.digitalservice.useid.integration.RedisTestContainerConfig
import de.governikus.autent.sdk.eidservice.tctoken.TCTokenType
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Tag("integration")
@Transactional
class EidAvailabilityServiceIntegrationTest() : RedisTestContainerConfig() {

    @Autowired
    private lateinit var eidAvailabilityRepository: EidAvailabilityRepository

    @Autowired
    private lateinit var eidAvailabilityService: EidAvailabilityService

    @BeforeAll
    fun setupBeforeAll() {
        mockkConstructor(EidService::class)
    }

    @BeforeEach
    fun setup() {
        eidAvailabilityRepository.deleteAll()
    }

    @AfterAll
    fun teardown() {
        eidAvailabilityRepository.deleteAll()
    }

    @Test
    fun `should store true if eIdService responds with no error`() {
        val mockTCToken = mockk<TCTokenType>()
        every { mockTCToken.refreshAddress } returns "https://www.foobar.com?sessionId=1234"
        every { anyConstructed<EidService>().getTcToken(any()) } returns mockTCToken

        eidAvailabilityService.checkEidServiceAvailability()
        val foundResults = eidAvailabilityRepository.findAll()

        assertThat(foundResults.toList()).hasSize(1)
        assertThat(foundResults.toList()[0].up).isTrue
    }

    @Test
    fun `should store false if eIdService responds with an exception`() {
        every { anyConstructed<EidService>().getTcToken(any()) } throws Exception("some error")

        eidAvailabilityService.checkEidServiceAvailability()
        val foundResults = eidAvailabilityRepository.findAll()

        assertThat(foundResults.toList()).hasSize(1)
        assertThat(foundResults.toList()[0].up).isFalse
    }
}
