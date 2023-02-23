package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.apikeys.ApiKeyAuthenticationFilter
import de.bund.digitalservice.useid.apikeys.MANAGE_IDENTIFICATION_SESSION_AUTHORITY
import de.bund.digitalservice.useid.identification.IDENTIFICATION_SESSIONS_BASE_PATH
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationManager: AuthenticationManager,
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests()
            .requestMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tc-token").permitAll()
            .requestMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens/*").permitAll()
            .requestMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/*/tokens").permitAll()
            .requestMatchers(HttpMethod.GET, "$IDENTIFICATION_SESSIONS_BASE_PATH/*/transaction-info").permitAll()
            .requestMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/**").authenticated()
            .requestMatchers("$IDENTIFICATION_SESSIONS_BASE_PATH/**").hasAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)
            .anyRequest().permitAll()
            .and().csrf().disable()
            .headers()
            .frameOptions().disable()
            .and()
            .addFilterAfter(
                SecurityHeadersFilter(contentSecurityPolicyProperties),
                FilterSecurityInterceptor::class.java, // Last filter in the Spring Security filter chain
            )
            .addFilterBefore(
                ApiKeyAuthenticationFilter(authenticationManager),
                AnonymousAuthenticationFilter::class.java,
            )
            .addFilterAfter(
                TenantIdFilter(contentSecurityPolicyProperties),
                FilterSecurityInterceptor::class.java, // Last filter in the Spring Security filter chain
            )
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .build()
    }
}
