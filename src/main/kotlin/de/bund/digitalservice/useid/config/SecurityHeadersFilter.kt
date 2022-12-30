package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.widget.WIDGET_PAGE
import org.springframework.http.HttpHeaders
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

class SecurityHeadersFilter(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) : WebFilter {

    private val widgetPagePath: PathPattern = PathPatternParser().parse("/$WIDGET_PAGE")
    private val listOfPages = listOf(widgetPagePath)

    override fun filter(
        serverWebExchange: ServerWebExchange,
        webFilterChain: WebFilterChain
    ): Mono<Void> {
        val request: ServerHttpRequest = serverWebExchange.request
        val response: ServerHttpResponse = serverWebExchange.response
        val pathIsValidWidgetPages = listOfPages.any { it.matches(request.path.pathWithinApplication()) }

        if (!pathIsValidWidgetPages) return webFilterChain.filter(serverWebExchange)

        val hostName: String? = request.queryParams.getFirst("hostname")
        val hostNameIsAllowed = hostName?.let { contentSecurityPolicyProperties.domainIsAllowed(it) }

        if (hostNameIsAllowed == true) {
            val securityHeaders = mapOf(
                "Content-Security-Policy" to contentSecurityPolicyProperties.getCSPHeaderValue(hostName),
                HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN to hostName
            )

            response.headers.setAll(securityHeaders)
        } else {
            response.headers.set(
                "Content-Security-Policy",
                contentSecurityPolicyProperties.getDefaultCSPHeaderValue()
            )
        }

        return webFilterChain.filter(serverWebExchange)
    }
}
