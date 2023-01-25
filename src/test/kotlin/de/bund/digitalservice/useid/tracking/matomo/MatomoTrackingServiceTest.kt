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

    val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
    val encodedUserAgent = "Mozilla%2F5.0%20%28Macintosh%3B%20Intel%20Mac%20OS%20X%2010_15_7%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F109.0.0.0%20Safari%2F537.36"

    @Test
    fun `constructEventURL should return correct URL with encoded query parameters`() {
        val e = MatomoEvent(this, "category", "action", "name", "sessionId", userAgent)
        val url = matomoTrackingService.constructEventURL(e)

        val siteId = trackingProperties.matomo.siteId
        val domain = trackingProperties.matomo.domain

        val expectedURL = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}&uid=${e.sessionId}&ua=$encodedUserAgent"
        assertEquals(expectedURL, url)
    }

    @Test
    fun `constructEventURL should return correct URL without sessionId and useragent`() {
        val e = MatomoEvent(this, "category", "action", "name", null, null)
        val url = matomoTrackingService.constructEventURL(e)

        val siteId = trackingProperties.matomo.siteId
        val domain = trackingProperties.matomo.domain

        val expectedURL = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}"
        assertEquals(expectedURL, url)
    }

    @Test
    fun `matomo tracking service should trigger web request and log event category, action and name and code 200`() {
        val matomoEvent = MatomoEvent(this, "log1", "log2", "log3", "log4", userAgent)
        applicationEventPublisher.publishEvent(matomoEvent)
        every { webRequests.POST(any()) } returns Mono.empty()
        verify { webRequests.POST(any()) }
    }
}
