package de.bund.digitalservice.useid.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService
import org.springframework.security.core.userdetails.User
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.server.SecurityWebFilterChain

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {

    @Value("\${api.user.username}")
    var username: String? = null

    @Value("\${api.user.password}")
    var password: String? = null

    @Bean
    fun springSecurityFilterChain(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http.authorizeExchange()
            .pathMatchers("/api/v1/identification/**").authenticated()
            .anyExchange().permitAll()
            .and().httpBasic()
            .and().csrf().disable()
            .headers().frameOptions().disable()
            .and().build()
    }

    @Bean
    fun userDetailsService(): MapReactiveUserDetailsService {
        val admin: UserDetails = User.builder().username("$username").password("{noop}$password").roles("USER").build()
        return MapReactiveUserDetailsService(admin)
    }
}
