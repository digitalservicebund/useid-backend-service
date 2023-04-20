package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.identification.IDENTIFICATIONS_BASE_PATH
import de.bund.digitalservice.useid.identification.IDENTIFICATIONS_OLD_BASE_PATH
import de.bund.digitalservice.useid.tenant.MANAGE_IDENTIFICATION_SESSION_AUTHORITY
import de.bund.digitalservice.useid.tenant.ResolveTenantFilter
import de.bund.digitalservice.useid.tenant.TenantAuthenticationFilter
import de.bund.digitalservice.useid.tenant.TenantProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.access.intercept.AuthorizationFilter
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val authenticationManager: AuthenticationManager,
    private val tenantProperties: TenantProperties,
) {
    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        return http.authorizeHttpRequests()
            .requestMatchers("$IDENTIFICATIONS_OLD_BASE_PATH/*/tc-token").permitAll()
            .requestMatchers("$IDENTIFICATIONS_OLD_BASE_PATH/*/tokens/*").permitAll()
            .requestMatchers("$IDENTIFICATIONS_OLD_BASE_PATH/*/tokens").permitAll()
            .requestMatchers(HttpMethod.GET, "$IDENTIFICATIONS_OLD_BASE_PATH/*/transaction-info").permitAll()
            .requestMatchers("$IDENTIFICATIONS_BASE_PATH/**").authenticated()
            .requestMatchers("$IDENTIFICATIONS_BASE_PATH/**").hasAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)
            .requestMatchers("$IDENTIFICATIONS_OLD_BASE_PATH/**").authenticated()
            .requestMatchers("$IDENTIFICATIONS_OLD_BASE_PATH/**").hasAuthority(MANAGE_IDENTIFICATION_SESSION_AUTHORITY)
            .anyRequest().permitAll()
            .and().csrf().disable()
            .headers()
            .frameOptions().disable()
            .and()
            .addFilterBefore(
                TenantAuthenticationFilter(authenticationManager),
                AnonymousAuthenticationFilter::class.java,
            )
            .addFilterAfter(
                ResolveTenantFilter(tenantProperties),
                AuthorizationFilter::class.java, // Last filter in the Spring Security filter chain
            )
            .addFilterAfter(
                WidgetSecurityHeadersFilter(),
                ResolveTenantFilter::class.java,
            )
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .httpBasic().disable()
            .formLogin().disable()
            .logout().disable()
            .build()
    }
}
