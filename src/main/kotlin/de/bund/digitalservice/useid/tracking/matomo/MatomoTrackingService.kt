package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
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
@Profile("!local")
@Service
class MatomoTrackingService(trackingProperties: TrackingProperties, private val webRequests: WebRequests) {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain

    fun constructEventURL(e: MatomoEvent): String {
        val session = e.sessionId?.let { "&uid=$it" } ?: ""
        val userAgent = e.userAgent?.let { "&ua=${UriUtils.encode(e.userAgent, UTF_8)}" } ?: ""
        // custom dimension for tenantId -> https://matomo.org/faq/reporting-tools/create-track-and-manage-custom-dimensions/
        val tenantId = e.tenantId?.let { "&dimension2=${UriUtils.encode(e.tenantId, UTF_8)}" } ?: ""
        val url = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}$session$userAgent$tenantId"
        log.debug { url }
        return url
    }

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
