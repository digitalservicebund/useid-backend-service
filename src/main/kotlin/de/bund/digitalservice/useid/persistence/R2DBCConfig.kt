package de.bund.digitalservice.useid.persistence

import org.springframework.context.annotation.Configuration
import org.springframework.data.r2dbc.repository.config.EnableR2dbcRepositories

@Configuration
@EnableR2dbcRepositories
internal class R2DBCConfig {
    // @Bean
    // fun initializer(@Qualifier("connectionFactory") connectionFactory: ConnectionFactory): ConnectionFactoryInitializer {
    //     val initializer = ConnectionFactoryInitializer()
    //     initializer.setConnectionFactory(connectionFactory)
    //     val resource = ResourceDatabasePopulator(ClassPathResource("schema.sql"))
    //     initializer.setDatabasePopulator(resource)
    //     return initializer
    // }
}
