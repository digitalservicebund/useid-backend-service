package de.bund.digitalservice.useid.config

import io.micrometer.core.instrument.Tag
import org.springframework.boot.actuate.metrics.web.reactive.server.WebFluxTagsContributor
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange

@Component
class CustomWebClientExchangeTagsProvider : WebFluxTagsContributor {
    override fun httpRequestTags(exchange: ServerWebExchange?, ex: Throwable?): MutableIterable<Tag> {
        val tags = mutableListOf<Tag>()
        if (exchange != null) {
            tags.add(Tag.of("tenant_id", exchange.attributes["tenant_id"].toString()))
        }
        return tags
    }
}
