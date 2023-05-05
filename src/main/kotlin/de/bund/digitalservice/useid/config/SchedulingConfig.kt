package de.bund.digitalservice.useid.config

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.scheduling.annotation.EnableScheduling
import javax.sql.DataSource

@ConditionalOnProperty(
    value = ["app.scheduling.enable"],
    havingValue = "true",
    matchIfMissing = true,
)
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "10m", defaultLockAtLeastFor = "1m")
class SchedulingConfig() {
    @Bean
    fun lockProvider(dataSource: DataSource): LockProvider? {
        return JdbcTemplateLockProvider(
            JdbcTemplateLockProvider.Configuration.builder()
                .withJdbcTemplate(JdbcTemplate(dataSource))
                .usingDbTime()
                .build(),
        )
    }
}
