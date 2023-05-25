package de.bund.digitalservice.useid.config

import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ModelAttribute

@ControllerAdvice
class GlobalControllerAdvice(private val applicationProperties: ApplicationProperties) {
    @ModelAttribute("baseUrl")
    fun baseUrlForTemplates(): String {
        return applicationProperties.baseUrl
    }
}
