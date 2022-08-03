package de.bund.digitalservice.useid.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "api")
class ApiProperties {
    var keys: List<String> = emptyList()
}
