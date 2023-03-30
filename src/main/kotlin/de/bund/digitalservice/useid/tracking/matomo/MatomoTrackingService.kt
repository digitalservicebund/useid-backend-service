package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import mu.KotlinLogging
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import kotlin.text.Charsets.UTF_8

/**
 * Service for sending tracking events to the matomo server.
 * The service implements an ApplicationListener for a custom MatomoEvent object/event.
 *
 * Documentation about the tracking api:
 * https://developer.matomo.org/api-reference/tracking-api
 *
 * Documentation about matomo events
 * https://matomo.org/guide/reports/event-tracking/
 */
@Service
class MatomoTrackingService(trackingProperties: TrackingProperties, private val webRequests: WebRequests) {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain
    private val dimensionIdTenant = trackingProperties.matomo.dimensionIdTenant

    fun constructEventURL(e: MatomoEvent): String {
        val session = e.sessionId?.let { "&uid=$it" } ?: ""
        val userAgent = e.userAgent?.let { "&ua=${encode(e.userAgent)}" } ?: ""
        // custom dimension for tenantId -> https://matomo.org/faq/reporting-tools/create-track-and-manage-custom-dimensions/
        val tenantId = e.tenantId?.let { "&dimension$dimensionIdTenant=${encode(e.tenantId)}" } ?: ""

        return UriComponentsBuilder
            .newInstance()
            .scheme("https")
            .host(domain)
            .path("?idsite=$siteId")
            .path("&rec=1")
            .path("&ca=1")
            .path("&e_c=${e.category}")
            .path("&e_a=${e.action}")
            .path("&e_n=${e.name}")
            .path(session)
            .path(userAgent)
            .path(tenantId)
            .build()
            .toUriString()
    }

    private fun encode(input: String): String = UriUtils.encode(input, UTF_8)

    @EventListener
    @Async
    fun sendEvent(e: MatomoEvent) {
        val eventUrl = constructEventURL(e)

        if (webRequests.POST(eventUrl)) {
            log.info("Tracking ok: $eventUrl")
        } else {
            log.error("Tracking failed: $eventUrl")
        }
    }
}
