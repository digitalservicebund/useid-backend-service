package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_ENDPOINT
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Bean
    fun springSecurityFilterChain(
        http: ServerHttpSecurity,
        authenticationManager: ReactiveAuthenticationManager,
        authenticationConverter: ServerAuthenticationConverter
    ): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("$IDENTIFICATION_SESSIONS_ENDPOINT/**").authenticated()
            .anyExchange().permitAll()
            .and().csrf().disable()
            .headers().frameOptions().disable()
            .and().addFilterAfter(
                authenticationFilter(authenticationManager, authenticationConverter),
                SecurityWebFiltersOrder.REACTOR_CONTEXT
            )
            .build()
    }

    @Bean
    fun authenticationFilter(manager: ReactiveAuthenticationManager, converter: ServerAuthenticationConverter): AuthenticationWebFilter? {
        val filter = AuthenticationWebFilter(manager)
        filter.setServerAuthenticationConverter(converter)
        return filter
    }
}
