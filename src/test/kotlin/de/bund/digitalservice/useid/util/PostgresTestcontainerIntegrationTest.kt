package de.bund.digitalservice.useid.util

import org.junit.jupiter.api.Tag
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.PostgreSQLContainer
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Tag("integration")
class PostgresTestcontainerIntegrationTest {
    companion object {
        @Container
        val postgresql = PostgresTestcontainer.instance!!.apply {
            withDatabaseName("testdb")
            withUsername("user")
            withPassword("password")
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun postgresqlProperties(registry: DynamicPropertyRegistry) {
            val r2dbcUrl = String.format(
                "r2dbc:postgresql://%s:%s/%s",
                postgresql.host,
                postgresql.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT),
                postgresql.databaseName
            )

            registry.add("spring.r2dbc.url") { r2dbcUrl }
            registry.add("spring.r2dbc.username") { postgresql.username }
            registry.add("spring.r2dbc.password") { postgresql.password }
            registry.add("spring.flyway.url") { postgresql.jdbcUrl }
            registry.add("spring.flyway.user") { postgresql.username }
            registry.add("spring.flyway.password") { postgresql.password }
        }
    }
}
