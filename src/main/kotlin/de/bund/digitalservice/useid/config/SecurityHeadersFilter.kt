package de.bund.digitalservice.useid.config

import de.bund.digitalservice.useid.tenant.REQUEST_ATTR_TENANT
import de.bund.digitalservice.useid.tenant.TenantProperties
import de.bund.digitalservice.useid.tenant.tenants.Tenant
import de.bund.digitalservice.useid.tenant.tenants.WidgetDefaultTenant
import de.bund.digitalservice.useid.widget.WIDGET_PAGE
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.http.server.PathContainer
import org.springframework.web.filter.OncePerRequestFilter
import org.springframework.web.util.pattern.PathPattern
import org.springframework.web.util.pattern.PathPatternParser
import java.util.UUID

internal const val HTTP_HEADER_CONTENT_SECURITY_POLICY = "Content-Security-Policy"
class SecurityHeadersFilter(
    private val tenantProperties: TenantProperties,
    private val contentSecurityPolicy: ContentSecurityPolicy,
) : OncePerRequestFilter() {

    private val widgetPagePath: PathPattern = PathPatternParser().parse("/$WIDGET_PAGE")
    private val listOfPages = listOf(widgetPagePath)

    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val pathIsValidWidgetPages = listOfPages.any {
            it.matches(PathContainer.parsePath(request.servletPath))
        }

        if (!pathIsValidWidgetPages) return filterChain.doFilter(request, response)

        val tenant = request.getAttribute(REQUEST_ATTR_TENANT) as Tenant
        if (tenant is WidgetDefaultTenant) {
            response.setHeader(HTTP_HEADER_CONTENT_SECURITY_POLICY, contentSecurityPolicy.getDefaultCSPHeaderValue())
        } else {
            tenant.cspNonce = UUID.randomUUID().toString()
            response.setHeader(HTTP_HEADER_CONTENT_SECURITY_POLICY, contentSecurityPolicy.getCSPHeaderValue(tenant.cspHost, tenant.cspNonce))
            response.setHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, tenant.cspHost)
            response.setHeader(HttpHeaders.VARY, HttpHeaders.ORIGIN)
        }

        return filterChain.doFilter(request, response)
    }
}
