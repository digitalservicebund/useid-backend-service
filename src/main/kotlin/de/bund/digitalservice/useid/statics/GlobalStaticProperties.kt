package de.bund.digitalservice.useid.statics

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "global-static")
@Validated
class GlobalStaticProperties {

    @Valid
    var errorView: ErrorView = ErrorView()

    class ErrorView {
        @Valid
        var localization: Localization = Localization()

        class Localization {
            @NotBlank
            lateinit var errorTitle: String

            @NotBlank
            lateinit var errorDescription: String

            @NotBlank
            lateinit var errorReportEmail: String

            @NotBlank
            lateinit var errorReportSubject: String

            @NotBlank
            lateinit var errorReportBody: String
        }
    }
}
