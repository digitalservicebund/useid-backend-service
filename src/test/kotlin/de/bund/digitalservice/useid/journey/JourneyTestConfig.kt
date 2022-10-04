package de.bund.digitalservice.useid.journey

import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.test.context.TestConfiguration

@TestConfiguration
@EnableConfigurationProperties(JourneyTestApplicationProperties::class)
class JourneyTestConfig
