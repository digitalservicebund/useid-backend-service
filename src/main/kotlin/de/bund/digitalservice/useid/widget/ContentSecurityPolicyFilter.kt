package de.bund.digitalservice.useid.widget

import de.bund.digitalservice.useid.config.ContentSecurityPolicyProperties
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import reactor.core.publisher.Mono

@Component
class ContentSecurityPolicyFilter(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) : WebFilter {

    private val widgetPagePath: PathPattern = PathPatternParser().parse(WIDGET_PAGE_PATH)
    private val incompatiblePagePath: PathPattern = PathPatternParser().parse(INCOMPATIBLE_PAGE_PATH)

    private val listOfPages: List<PathPattern> = listOf(widgetPagePath, incompatiblePagePath)

    override fun filter(
        serverWebExchange: ServerWebExchange,
        webFilterChain: WebFilterChain
    ): Mono<Void> {
        val request: ServerHttpRequest = serverWebExchange.request
        val response: ServerHttpResponse = serverWebExchange.response

        val allowedHost = contentSecurityPolicyProperties.domainIsAllowed(request.uri.host)
        val filteredPages = listOfPages.any { it.matches(request.path.pathWithinApplication()) }

        if (filteredPages && allowedHost) {
            response.headers.set(
                "Content-Security-Policy",
                contentSecurityPolicyProperties.getCSPHeaderValue(request.uri.host)
            )
        } else {
            response.headers.set(
                "Content-Security-Policy",
                contentSecurityPolicyProperties.getDefaultCSPHeaderValue()
            )
        }

        return webFilterChain.filter(serverWebExchange)
    }
}
