package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.TrackingWebRequests
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!local")
/**
 * Service for sending tracking events to the matomo server.
 * The service implements an ApplicationListener for a custom MatomoEvent object/event.
 *
 * Documentation about the tracking api:
 * https://developer.matomo.org/api-reference/tracking-api
 */
@Service
class MatomoTrackingService(trackingProperties: TrackingProperties, private val trackingWebRequests: TrackingWebRequests) : ApplicationListener<MatomoEvent> {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain

    private fun constructURL(e: MatomoEvent): String {
        // https://your-matomo-domain.example/matomo.php?yourQueryParams=2
        val url = "$domain?idsite=$siteId&rec=1&ca=1&e_c=${e.category}&e_a=${e.action}&e_n=${e.name}"
        log.debug { url }
        return url
    }

    override fun onApplicationEvent(e: MatomoEvent) {
        log.debug("Received MatomoEvent e_c=${e.category}&e_a=${e.action}&e_n=${e.name}")
        val url = constructURL(e)
        trackingWebRequests.GET(url)
    }
}
