package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
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
class SecurityConfig(
    private val authenticationManager: ReactiveAuthenticationManager,
    private val authenticationConverter: ServerAuthenticationConverter,
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) {
    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tc-token").permitAll()
            .pathMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens/*").permitAll()
            .pathMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens").permitAll()
            .pathMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/**").authenticated()
            .anyExchange().permitAll()
            .and().csrf().disable()
            .headers()
            .frameOptions().disable()
            .and().addFilterAfter(
                authenticationFilter(),
                SecurityWebFiltersOrder.REACTOR_CONTEXT
            )
            .addFilterAfter(
                SecurityHeadersFilter(contentSecurityPolicyProperties),
                SecurityWebFiltersOrder.LAST
            )
            .build()
    }

    @Bean
    fun authenticationFilter(): AuthenticationWebFilter? {
        val filter = AuthenticationWebFilter(authenticationManager)
        filter.setServerAuthenticationConverter(authenticationConverter)
        return filter
    }
}
