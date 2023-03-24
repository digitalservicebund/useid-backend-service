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
        val tenant: Tenant? = if (request.servletPath.equals(REQUEST_PATH_WIDGET)) {
            val host = request.getParameter("hostname") ?: ""
            tenantProperties.findByAllowedHost(host)
        } else if (request.getParameter(PARAM_NAME_TENANT_ID) != null) {
            tenantProperties.findByTenantId(request.getParameter(PARAM_NAME_TENANT_ID))
        } else {
            val defaultTenant = Tenant()
            defaultTenant.id = "unknown"
            defaultTenant
        }

        request.setAttribute(REQUEST_ATTR_TENANT, tenant)
        return filterChain.doFilter(request, response)
    }
}
