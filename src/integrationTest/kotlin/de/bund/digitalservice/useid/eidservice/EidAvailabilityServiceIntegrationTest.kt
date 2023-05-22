package de.bund.digitalservice.useid.eidservice

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.identification.mockTcToken
import de.bund.digitalservice.useid.integration.RedisTestContainerConfig
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import io.mockk.every
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Transactional
class EidAvailabilityServiceIntegrationTest : RedisTestContainerConfig() {

    @Autowired
    private lateinit var eidAvailabilityRepository: EidAvailabilityRepository

    @Autowired
    private lateinit var eidAvailabilityService: EidAvailabilityService

    @MockkBean
    private lateinit var soapHandler: SoapHandler

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
        mockTcToken(soapHandler, "https://www.foobar.com?sessionId=1234")

        eidAvailabilityService.checkEidServiceAvailability()
        val foundResults = eidAvailabilityRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(true))
    }

    @Test
    fun `should store false if eIdService responds with an exception`() {
        every { soapHandler.getTcToken(any(), any()) } throws Exception("some error")

        eidAvailabilityService.checkEidServiceAvailability()
        val foundResults = eidAvailabilityRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(false))
    }
}
