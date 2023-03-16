package de.bund.digitalservice.useid.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter

@Configuration
class XmlConverterConfig {
    @Bean
    fun jaxb2RootElementHttpMessageConverter() = Jaxb2RootElementHttpMessageConverter()
}
