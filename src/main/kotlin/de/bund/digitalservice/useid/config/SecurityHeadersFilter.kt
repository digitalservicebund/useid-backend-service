package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.widget.WIDGET_PAGE
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser

class SecurityHeadersFilter(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties,
) : OncePerRequestFilter() {

    private val widgetPagePath: PathPattern = PathPatternParser().parse("/$WIDGET_PAGE")
    private val listOfPages = listOf(widgetPagePath)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val pathIsValidWidgetPages = listOfPages.any {
            it.matches(PathContainer.parsePath(request.servletPath))
        }

        if (!pathIsValidWidgetPages) return filterChain.doFilter(request, response)

        val hostName: String? = request.getParameter("hostname")
        val hostNameIsAllowed = hostName?.let { contentSecurityPolicyProperties.domainIsAllowed(it) }

        if (hostNameIsAllowed == true) {
            response.setHeader("Content-Security-Policy", contentSecurityPolicyProperties.getCSPHeaderValue(hostName))
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, hostName)
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        } else {
            response.setHeader("Content-Security-Policy", contentSecurityPolicyProperties.getDefaultCSPHeaderValue())
        }

        return filterChain.doFilter(request, response)
    }
}
