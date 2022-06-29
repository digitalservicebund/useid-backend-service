package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.filter.SimpleAuthenticationFilter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.util.matcher.PathPatternParserServerWebExchangeMatcher

@Configuration
@EnableWebFluxSecurity
class SecurityConfig {
    @Order(Ordered.HIGHEST_PRECEDENCE)
    @Bean
    fun apiHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .securityMatcher(PathPatternParserServerWebExchangeMatcher("/api/**"))
            .authorizeExchange {
                it.anyExchange()
                    .permitAll()
                    .and()
                    .addFilterBefore(SimpleAuthenticationFilter(), SecurityWebFiltersOrder.AUTHENTICATION)
            }.csrf {
                it.disable()
            }.build()
    }

    @Bean
    fun webHttpSecurity(http: ServerHttpSecurity): SecurityWebFilterChain {
        return http
            .headers {
                it.frameOptions().disable()
            }.authorizeExchange {
                it.anyExchange().permitAll()
            }.csrf {
                it.disable()
            }.build()
    }
}
