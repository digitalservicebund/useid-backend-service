package de.bund.digitalservice.useid.panstar

import de.governikus.panstar.sdk.soap.configuration.SoapConfiguration
import de.governikus.panstar.sdk.soap.handler.SoapHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SoapHandlerConfig {
    @Bean
    fun soapHandler(soapConfiguration: SoapConfiguration): SoapHandler {
        return SoapHandler(soapConfiguration)
    }
}
