package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationManager: AuthenticationManager,
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
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
            // .addFilterAfter(
            //     SecurityHeadersFilter(contentSecurityPolicyProperties),
            //     FilterSecurityInterceptor::class.java // Last filter in the Spring Security filter chain
            // )
            // .addFilterBefore(
            //     ApiKeyAuthenticationFilter(authenticationManager, "$IDENTIFICATION_SESSIONS_BASE_PATH/**"),
            //     AnonymousAuthenticationFilter::class.java
            // )
            .build()
    }
}
