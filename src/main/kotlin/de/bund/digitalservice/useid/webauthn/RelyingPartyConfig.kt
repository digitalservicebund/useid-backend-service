package de.bund.digitalservice.useid.webauthn

import com.yubico.webauthn.RelyingParty
import com.yubico.webauthn.data.RelyingPartyIdentity
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

// documentation: https://github.com/Yubico/java-webauthn-server#2-instantiate-a-relyingparty
@Configuration
class RelyingPartyConfig {

    @Bean
    fun relyingParty(): RelyingParty {
        val rpIdentity = RelyingPartyIdentity.builder()
            .id("1f24-2003-d6-cf04-9000-80be-b1fd-2410-2a4a.eu.ngrok.io") // Set this to a parent domain that covers all subdomains
            // where users' credentials should be valid
            .name("Example Application")
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(UserCredentialRepository())
            .build()
    }
}
