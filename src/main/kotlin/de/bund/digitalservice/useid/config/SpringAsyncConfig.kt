package de.bund.digitalservice.useid.config

import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.EnableAsync

@Profile("!test")
@Configuration
@EnableAsync
class SpringAsyncConfig
