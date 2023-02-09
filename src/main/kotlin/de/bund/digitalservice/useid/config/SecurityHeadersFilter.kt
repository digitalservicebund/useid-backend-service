package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.widget.WIDGET_PAGE
import org.springframework.http.HttpHeaders
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.GenericFilterBean
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import javax.servlet.FilterChain
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class SecurityHeadersFilter(
    private val contentSecurityPolicyProperties: ContentSecurityPolicyProperties
) : GenericFilterBean() {

    private val widgetPagePath: PathPattern = PathPatternParser().parse("/$WIDGET_PAGE")
    private val listOfPages = listOf(widgetPagePath)

    override fun doFilter(request: ServletRequest, response: ServletResponse, chain: FilterChain) {
        val httpRequest = request as HttpServletRequest
        val httpResponse = response as HttpServletResponse
        val pathIsValidWidgetPages = listOfPages.any { it.matches(PathContainer.parsePath(httpRequest.pathInfo)) }

        if (!pathIsValidWidgetPages) return chain.doFilter(request, response)

        val hostName: String? = httpRequest.getParameter("hostname")
        val hostNameIsAllowed = hostName?.let { contentSecurityPolicyProperties.domainIsAllowed(it) }

        if (hostNameIsAllowed == true) {
            httpResponse.setHeader("Content-Security-Policy", contentSecurityPolicyProperties.getCSPHeaderValue(hostName))
            httpResponse.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, hostName)
            httpResponse.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        } else {
            httpResponse.setHeader("Content-Security-Policy", contentSecurityPolicyProperties.getDefaultCSPHeaderValue())
        }

        return chain.doFilter(request, response)
    }
}
