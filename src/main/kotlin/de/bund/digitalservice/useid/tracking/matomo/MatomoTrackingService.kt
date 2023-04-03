package de.bund.digitalservice.useid.tracking.matomo

import de.bund.digitalservice.useid.tracking.TrackingProperties
import de.bund.digitalservice.useid.tracking.WebRequests
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.web.util.UriComponentsBuilder
import org.springframework.web.util.UriUtils
import java.util.Optional
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
@Profile("!test") // tests do not need to fire tracking events
class MatomoTrackingService(trackingProperties: TrackingProperties, private val webRequests: WebRequests) {

    private val log = KotlinLogging.logger {}
    private val siteId = trackingProperties.matomo.siteId
    private val domain = trackingProperties.matomo.domain
    private val dimensionIdTenant = trackingProperties.matomo.dimensionIdTenant

    fun constructEventUrl(e: MatomoEvent): String {
        return UriComponentsBuilder
            .newInstance()
            .scheme("https")
            .host(domain)
            .queryParam("idsite", siteId)
            .queryParam("rec", 1)
            .queryParam("ca", 1)
            .queryParamIfPresent("e_c", uriEncode(e.category))
            .queryParamIfPresent("e_a", uriEncode(e.action))
            .queryParamIfPresent("e_n", uriEncode(e.name))
            .queryParamIfPresent("uid", uriEncode(e.sessionId))
            .queryParamIfPresent("ua", uriEncode(e.userAgent))
            // custom dimension for tenantId -> https://matomo.org/faq/reporting-tools/create-track-and-manage-custom-dimensions/
            .queryParamIfPresent("dimension$dimensionIdTenant", uriEncode(e.tenantId))
            .build()
            .toString()
    }

    // UriComponentBuilder does not support query param encoding
    private fun uriEncode(input: String?): Optional<String> {
        if (input == null) return Optional.empty()
        return Optional.of<String>(UriUtils.encode(input, UTF_8))
    }

    @EventListener
    @Async
    fun sendEvent(e: MatomoEvent) {
        val eventUrl = constructEventUrl(e)

        if (webRequests.POST(eventUrl)) {
            log.info("Tracking ok: $eventUrl")
        } else {
            log.error("Tracking failed: $eventUrl")
        }
    }
}
