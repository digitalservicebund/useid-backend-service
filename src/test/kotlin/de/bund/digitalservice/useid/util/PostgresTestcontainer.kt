package de.bund.digitalservice.useid.util

import org.testcontainers.containers.PostgreSQLContainer

class PostgresTestcontainer private constructor() : PostgreSQLContainer<PostgresTestcontainer?>(IMAGE_VERSION) {

    companion object {
        private const val IMAGE_VERSION = "postgres:12.12-alpine"
        private var container: PostgresTestcontainer? = null

        val instance: PostgresTestcontainer?
            get() {
                if (container == null) {
                    container = PostgresTestcontainer()
                }
                return container
            }
    }

    override fun start() {
        super.start()

        val r2dbcUrl = String.format(
            "r2dbc:postgresql://%s:%s/%s",
            this.host,
            this.getMappedPort(POSTGRESQL_PORT),
            this.databaseName
        )

        System.setProperty("spring.r2dbc.url", r2dbcUrl)
        System.setProperty("spring.r2dbc.username", this.username)
        System.setProperty("spring.r2dbc.password", this.password)
        System.setProperty("spring.flyway.url", this.jdbcUrl)
        System.setProperty("spring.flyway.user", this.username)
        System.setProperty("spring.flyway.password", this.password)
    }

    override fun stop() {
        // do nothing, JVM handles shut down
    }
}
