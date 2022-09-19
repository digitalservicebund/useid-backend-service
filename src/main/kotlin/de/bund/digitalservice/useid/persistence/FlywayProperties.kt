package de.bund.digitalservice.useid.persistence

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "spring.flyway")
@Validated
class FlywayProperties {
    @NotBlank
    lateinit var url: String

    @NotBlank
    lateinit var user: String

    @NotBlank
    lateinit var password: String
}
