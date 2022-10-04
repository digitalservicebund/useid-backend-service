package de.bund.digitalservice.useid.persistence

import org.flywaydb.core.Flyway
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class FlywayConfig(private val flywayProperties: FlywayProperties) {
    @Bean(initMethod = "migrate")
    fun flyway(): Flyway {
        return Flyway(
            Flyway.configure()
                .dataSource(flywayProperties.url, flywayProperties.user, flywayProperties.password)
                .baselineOnMigrate(true)
                .baselineVersion("0.0")
        )
    }
}
