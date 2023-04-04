package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

@Tag("test")
class MatomoTrackingServiceTest {

    private val userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/109.0.0.0 Safari/537.36"
    private val encodedUserAgent = "Mozilla%2F5.0%20%28Macintosh%3B%20Intel%20Mac%20OS%20X%2010_15_7%29%20AppleWebKit%2F537.36%20%28KHTML%2C%20like%20Gecko%29%20Chrome%2F109.0.0.0%20Safari%2F537.36"

    val matomo = TrackingProperties.Matomo().apply {
        siteId = "1"
        domain = "test"
        dimensionIdTenant = "3"
    }

    private val trackingProperties = mockk<TrackingProperties>()
    private val webRequests = mockk<WebRequests>()

    private var matomoTrackingService: MatomoTrackingService? = null

    @BeforeAll
    fun beforeAll() {
        every { trackingProperties.matomo } returns matomo
        matomoTrackingService = MatomoTrackingService(trackingProperties, webRequests)
    }

    @AfterAll
    fun afterAll() {
        unmockkAll()
    }

    @Test
    fun `constructEventUrl should return correct URL with encoded query parameters`() {
        val e = MatomoEvent(this, "category", "action", "name", "sessionId", userAgent, "tenantFoo")
        val url = matomoTrackingService?.constructEventUrl(e)

        val expectedURL = "https://${matomo.domain}?idsite=${matomo.siteId}&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}&uid=${e.sessionId}&ua=$encodedUserAgent&dimension${matomo.dimensionIdTenant}=tenantFoo"
        assertEquals(expectedURL, url)
    }

    @Test
    fun `constructEventUrl should return correct URL when passing null for all optional parameters`() {
        val e = MatomoEvent(this, "category", "action", "name", null, null, null)
        val url = matomoTrackingService?.constructEventUrl(e)

        val expectedURL = "https://${matomo.domain}?idsite=${matomo.siteId}&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}"
        assertEquals(expectedURL, url)
    }

    @Test
    fun `matomo tracking service should trigger web request`() {
        // Given
        val matomoEvent = MatomoEvent(this, "log1", "log2", "log3", "log4", userAgent, null)
        every { webRequests.POST(any()) } returns false

        // When
        matomoTrackingService?.sendEvent(matomoEvent)

        // Then
        verify { webRequests.POST(any()) }
    }
}
