package de.bund.digitalservice.useid.tenant

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import mu.KotlinLogging
import org.springframework.web.filter.OncePerRequestFilter

internal const val REQUEST_ATTR_TENANT = "tenant"
internal const val PARAM_NAME_TENANT_ID = "tenant_id"
internal const val PARAM_NAME_HOSTNAME = "hostname"
internal const val REQUEST_PATH_WIDGET = "/widget"

class ResolveTenantFilter(
    private val tenantProperties: TenantProperties,
) : OncePerRequestFilter() {

    private val log = KotlinLogging.logger {}
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        var resolvedTenant: Tenant? = null
        if (request.servletPath.equals(REQUEST_PATH_WIDGET)) {
            val host = request.getParameter(PARAM_NAME_HOSTNAME)
                ?: return response.sendError(HttpServletResponse.SC_BAD_REQUEST, "hostname parameter required")
            resolvedTenant = tenantProperties.findByAllowedHost(host)
                ?: return response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "given hostname value is forbidden")
            resolvedTenant.cspHost = host
        }
        // tenant_id parameter is set in widget
        else if (request.getParameter(PARAM_NAME_TENANT_ID) != null) {
            val id = request.getParameter(PARAM_NAME_TENANT_ID)
            resolvedTenant = tenantProperties.findByTenantId(id) ?: kotlin.run {
                log.info("could not find tenant for incoming id $id in ${request.requestURI}")
                null
            }
        }

        if (resolvedTenant != null) {
            request.setAttribute(REQUEST_ATTR_TENANT, resolvedTenant)
        }
        return filterChain.doFilter(request, response)
    }
}
