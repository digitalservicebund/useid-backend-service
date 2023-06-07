// PROTOTYPE FILE

package de.bund.digitalservice.useid.credentials

import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity
import de.bund.digitalservice.useid.config.ApplicationProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.net.URI

// documentation: https://github.com/Yubico/java-webauthn-server#2-instantiate-a-relyingparty
@Configuration
@ConditionalOnProperty(name = ["features.desktop-solution-enabled"], havingValue = "true")
class RelyingPartyConfig(val credentialMockDatasource: CredentialMockDatasource, val applicationProperties: ApplicationProperties) {

    @Bean
    fun relyingParty(): RelyingParty {
        val rpIdentity = RelyingPartyIdentity.builder()
            .id(URI(applicationProperties.baseUrl).host) // Set this to a parent domain that covers all subdomains where users' credentials should be valid
            .name("BundesIdent")
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(credentialMockDatasource)
            .build()
    }
}
