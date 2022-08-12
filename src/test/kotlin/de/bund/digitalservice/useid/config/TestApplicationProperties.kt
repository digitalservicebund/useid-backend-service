package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.util.YamlPropertySourceFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.PropertySource

@EnableConfigurationProperties(TestApplicationProperties::class)
@PropertySource("classpath:application.yaml", factory = YamlPropertySourceFactory::class)
@ConfigurationProperties(prefix = "application")
class TestApplicationProperties {
    var staging: StagingProperties? = null

    class StagingProperties {
        lateinit var url: String
    }
}
