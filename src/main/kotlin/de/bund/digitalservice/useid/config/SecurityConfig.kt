package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationFilter
import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.authentication.AuthenticationWebFilter
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter

@Configuration
@EnableWebSecurity
class SecurityConfig(
        private val authenticationManager: AuthenticationManager,
        private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) {
    @Bean
    fun springSecurityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeRequests()
            .antMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tc-token").permitAll()
            .antMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens/*").permitAll()
            .antMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens").permitAll()
            .antMatchers(HttpMethod.GET, "$IDENTIFICATION_SESSIONS_BASE_PATH/*/transaction-info").permitAll()
            .antMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/**").authenticated()
            .anyRequest().permitAll()
            .and().csrf().disable()
            .headers()
            .frameOptions().disable()
            .and()
            .addFilterAfter(
                SecurityHeadersFilter(contentSecurityPolicyProperties),
                SecurityWebFiltersOrder.LAST
            )
            .addFilterBefore(
                ApiKeyAuthenticationFilter(authenticationManager),
                AnonymousAuthenticationFilter.class
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
