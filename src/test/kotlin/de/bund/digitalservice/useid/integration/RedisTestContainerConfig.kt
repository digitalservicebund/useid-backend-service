package de.bund.digitalservice.useid.integration

import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
abstract class RedisTestContainerConfig {

    companion object {

        @JvmStatic
        var redisContainer = GenericContainer<Nothing>("redis:latest").apply {
            withExposedPorts(6379)
            withReuse(true)
            start()
        }

        @JvmStatic
        @DynamicPropertySource
        fun properties(registry: DynamicPropertyRegistry) {
            registry.add("spring.data.redis.host", redisContainer::getHost)
            registry.add("spring.data.redis.port", redisContainer::getFirstMappedPort)
        }
    }
}
