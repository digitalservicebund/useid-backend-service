package de.bund.digitalservice.useid.tenant

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.filter.OncePerRequestFilter

internal const val REQUEST_ATTR_TENANT = "tenant"
internal const val PARAM_NAME_TENANT_ID = "tenant_id"
internal const val REQUEST_PATH_WIDGET = "/widget"

class TenantFilter(
    private val tenantProperties: TenantProperties,
) : OncePerRequestFilter() {
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val fallbackTenant = Tenant().apply {
            id = "unknown"
        }

        val tenant =
            if (request.servletPath.equals(REQUEST_PATH_WIDGET)) {
                val host = request.getParameter("hostname") ?: ""
                tenantProperties.findByAllowedHost(host) ?: fallbackTenant
            } else if (request.getParameter(PARAM_NAME_TENANT_ID) != null) {
                val id = request.getParameter(PARAM_NAME_TENANT_ID)
                tenantProperties.findByTenantId(id) ?: fallbackTenant
            } else {
                fallbackTenant
            }

        request.setAttribute(REQUEST_ATTR_TENANT, tenant)
        return filterChain.doFilter(request, response)
    }
}
