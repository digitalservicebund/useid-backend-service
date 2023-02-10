package de.bund.digitalservice.useid.apikeys

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "api")
@Validated
class ApiProperties {
    @Valid
    var apiKeys: List<ApiKey> = emptyList()

    class ApiKey {
        @NotBlank
        lateinit var keyValue: String

        @NotBlank
        lateinit var refreshAddress: String

        var dataGroups: List<String> = emptyList()
    }
}