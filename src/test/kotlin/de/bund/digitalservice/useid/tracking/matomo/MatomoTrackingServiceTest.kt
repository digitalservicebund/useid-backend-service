package de.bund.digitalservice.useid.tracking.matomo

import com.ninjasquad.springmockk.MockkBean
import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import de.bund.digitalservice.useid.util.PostgresTestcontainerIntegrationTest
import io.mockk.every
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.context.junit.jupiter.SpringExtension
import reactor.core.publisher.Mono

@ExtendWith(value = [OutputCaptureExtension::class, SpringExtension::class])
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class MatomoTrackingServiceTest : PostgresTestcontainerIntegrationTest() {

    @Autowired
    private lateinit var matomoTrackingService: MatomoTrackingService

    @Autowired
    private lateinit var trackingProperties: TrackingProperties

    @Autowired
    private lateinit var applicationEventPublisher: ApplicationEventPublisher

    @MockkBean
    private lateinit var webRequests: WebRequests

    @Test
    fun `constructEventURL should return correct URL with query parameters`(output: CapturedOutput) {
        val e = MatomoEvent(this, "category", "action", "name")
        val url = matomoTrackingService.constructEventURL(e)

        val siteId = trackingProperties.matomo.siteId
        val domain = trackingProperties.matomo.domain
        val expectedURL = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}"
        assertEquals(expectedURL, url)
    }

    @Test
    fun `matomo tracking service should trigger web request and log event category, action and name and code 200`(output: CapturedOutput) {
        val matomoEvent = MatomoEvent(this, "log1", "log2", "log3")
        applicationEventPublisher.publishEvent(matomoEvent)
        every { webRequests.POST(any()) } returns Mono.empty()
        verify { webRequests.POST(any()) }
    }
}
