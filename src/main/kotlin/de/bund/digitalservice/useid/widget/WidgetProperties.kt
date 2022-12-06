package de.bund.digitalservice.useid.widget

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component
import org.springframework.validation.annotation.Validated
import javax.validation.Valid
import javax.validation.constraints.NotBlank

@Component
@ConfigurationProperties(prefix = "widget")
@Validated
class WidgetProperties {
    @Valid
    var metaTag: MetaTag = MetaTag()

    @Valid
    var mainView: MainView = MainView()

    @Valid
    var errorView: ErrorView = ErrorView()

    class MetaTag {
        @NotBlank
        lateinit var headerTitle: String
    }

    class MainView {
        @Valid
        var localization: Localization = Localization()

        @Valid
        var mobileUrl: MobileUrl = MobileUrl()

        class MobileUrl {
            @NotBlank
            lateinit var appStoreUrl: String

            @NotBlank
            lateinit var playStoreUrl: String

            @NotBlank
            lateinit var dataPrivacy: String
        }

        class Localization {
            @NotBlank
            lateinit var headlineImageAlt: String

            @NotBlank
            lateinit var appIconImageAlt: String

            @NotBlank
            lateinit var stepOneAlt: String

            @NotBlank
            lateinit var stepTwoAlt: String

            @NotBlank
            lateinit var headlineTitleTop: String

            @NotBlank
            lateinit var headlineTitleBottom: String

            @NotBlank
            lateinit var downloadTitle: String

            @NotBlank
            lateinit var appStoreAlt: String

            @NotBlank
            lateinit var playStoreAlt: String

            @NotBlank
            lateinit var identificationButton: String

            @NotBlank
            lateinit var identificationButtonTitle: String

            @NotBlank
            lateinit var dataPrivacyButton: String
        }
    }

    class ErrorView {
        @Valid
        var incompatible: Incompatible = Incompatible()

        @Valid
        var fallback: Fallback = Fallback()

        class Incompatible {
            @Valid
            var localization: Localization = Localization()
        }

        class Localization {
            @NotBlank
            lateinit var headlineTitle: String

            @NotBlank
            lateinit var headlineImageAlt: String

            @NotBlank
            lateinit var requirementTitle: String

            @NotBlank
            lateinit var androidTitle: String

            @NotBlank
            lateinit var androidRequirement: String

            @NotBlank
            lateinit var iphoneTitle: String

            @NotBlank
            lateinit var iphoneRequirement: String
        }

        class Fallback {
            @Valid
            var localization: LocalizationFallback = LocalizationFallback()
        }

        class LocalizationFallback {
            @NotBlank
            lateinit var iconAlt: String

            @NotBlank
            lateinit var errorTitle: String

            @NotBlank
            lateinit var errorText: String
        }
    }
}
