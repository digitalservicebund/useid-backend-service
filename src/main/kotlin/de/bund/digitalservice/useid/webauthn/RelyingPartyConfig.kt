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
            .id("example.com") // Set this to a parent domain that covers all subdomains
            // where users' credentials should be valid
            .name("Example Application")
            .build()

        return RelyingParty.builder()
            .identity(rpIdentity)
            .credentialRepository(UserCredentialRepository())
            .build()
    }
}
