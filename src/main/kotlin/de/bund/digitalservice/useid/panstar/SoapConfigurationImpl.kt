package de.bund.digitalservice.useid.panstar

import de.governikus.panstar.sdk.soap.configuration.SoapConfiguration
import de.governikus.panstar.sdk.soap.configuration.SoapEidServerConfiguration
import de.governikus.panstar.sdk.soap.configuration.SoapKeyMaterial
import org.springframework.stereotype.Component

@Component
class SoapConfigurationImpl(
    private val keyMaterial: SoapKeyMaterial,
    private val serverConfig: SoapEidServerConfiguration,
) :
    SoapConfiguration {
    override fun getSoapKeyMaterial(): SoapKeyMaterial {
        return keyMaterial
    }

    override fun getSoapEidServerConfiguration(): SoapEidServerConfiguration {
        return serverConfig
    }
}
