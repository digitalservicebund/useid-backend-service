package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.yaml.YamlPropertySourceFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.PropertySource

/**
 * This configuration class is used to provide test configuration properties for test cases.
 */
@EnableConfigurationProperties(TestApplicationProperties::class)
@PropertySource("classpath:application-journey-test.yaml", factory = YamlPropertySourceFactory::class)
@ConfigurationProperties(prefix = "application")
class TestApplicationProperties {
    var staging: StagingProperties? = null

    class StagingProperties {
        lateinit var url: String
        lateinit var apiKey: String
    }
}
