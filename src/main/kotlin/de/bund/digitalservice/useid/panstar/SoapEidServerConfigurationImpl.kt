package de.bund.digitalservice.useid.panstar

import de.governikus.panstar.sdk.soap.configuration.SoapEidServerConfiguration
import org.springframework.stereotype.Component

@Component
class SoapEidServerConfigurationImpl(private val panstarProperties: PanstarProperties) : SoapEidServerConfiguration {
    override fun getSoapEndpointUrl(): String {
        return panstarProperties.url
    }
}
