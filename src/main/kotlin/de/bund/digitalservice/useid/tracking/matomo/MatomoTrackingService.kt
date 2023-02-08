package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
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
class MatomoTrackingService(trackingProperties: TrackingProperties, private val webRequests: WebRequests) : ApplicationListener<MatomoEvent> {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain

    fun constructEventURL(e: MatomoEvent): String {
        val session = e.sessionId?.let { "&uid=$it" } ?: ""
        val userAgent = e.userAgent?.let { "&ua=${UriUtils.encode(e.userAgent, UTF_8)}" } ?: ""
        val url = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}$session$userAgent"
        log.debug { url }
        return url
    }

    private fun sendEvent(e: MatomoEvent) {
        val url = constructEventURL(e)

        if (webRequests.POST(url)) {
            log.info("200, successfully tracked: $url")
        } else {
            log.error("500, tracking failed for: $url")
        }
    }

    override fun onApplicationEvent(e: MatomoEvent) = sendEvent(e)
}
