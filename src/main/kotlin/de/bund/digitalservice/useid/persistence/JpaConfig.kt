package de.bund.digitalservice.useid.persistence

import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableJpaRepositories(basePackages = ["de.bund.digitalservice.useid"])
@EnableJpaAuditing
@EnableTransactionManagement
internal class JpaConfig
