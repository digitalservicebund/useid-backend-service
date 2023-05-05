package de.bund.digitalservice.useid.eidservice

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.identification.IdentificationSessionService
import io.mockk.every
import net.javacrumbs.shedlock.core.LockAssert
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
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
class EidServiceHealthTestSchedulerIntegrationTest() {

    @Autowired
    private lateinit var eidServiceRepository: EidServiceRepository

    @Autowired
    private lateinit var eidServiceHealthTestScheduler: EidServiceHealthTestScheduler

    @MockkBean
    private lateinit var identificationSessionService: IdentificationSessionService

    @BeforeAll
    fun setupBeforeAll() {
        eidServiceRepository.deleteAll()
    }

    @BeforeEach
    fun setup() {
        eidServiceRepository.deleteAll()
    }

    @AfterAll
    fun teardown() {
        eidServiceRepository.deleteAll()
        LockAssert.TestHelper.makeAllAssertsPass(false)
    }

    @Test
    @SchedulerLock(name = "eIdServiceHealthTest")
    fun `should store true if eIdService responds with no error`() {
        every { identificationSessionService.startSession(any(), any(), any()) } returns "https://someTcTokenUrl.com"

        eidServiceHealthTestScheduler.checkEIDServiceAvailability()
        val foundResults = eidServiceRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(true))
    }

    @Test
    @SchedulerLock(name = "eIdServiceHealthTest")
    fun `should store false if eIdService responds with an exception`() {
        every { identificationSessionService.startSession(any(), any(), any()) } throws Exception("some error")

        eidServiceHealthTestScheduler.checkEIDServiceAvailability()
        val foundResults = eidServiceRepository.findAll()

        assertThat(foundResults.toList().size, equalTo(1))
        assertThat(foundResults.toList()[0].up, equalTo(false))
    }
}
