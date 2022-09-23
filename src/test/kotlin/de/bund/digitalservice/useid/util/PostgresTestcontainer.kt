package de.bund.digitalservice.useid.util

import org.testcontainers.containers.PostgreSQLContainer

class PostgresTestcontainer private constructor(dockerImageVersion: String) : PostgreSQLContainer<PostgresTestcontainer?>(dockerImageVersion) {
    companion object {
        private var container: PostgresTestcontainer? = null
        val instance: PostgresTestcontainer?
            get() {
                if (container == null) {
                    container = PostgresTestcontainer("postgres:12.12-alpine")
                }
                return container
            }
    }
}
