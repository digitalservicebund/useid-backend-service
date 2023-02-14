package de.bund.digitalservice.useid.apikeys

import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter
import javax.servlet.FilterChain
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

class ApiKeyAuthenticationFilter(private val authenticationManager: AuthenticationManager) :
    OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader: String? = extractAuthHeader(request)
        val token = authHeader?.substring(AUTH_HEADER_VALUE_PREFIX.length)
        val authentication = token?.let { authenticationManager.authenticate(ApiKeyAuthenticationToken(token)) }

        if (authentication?.isAuthenticated == true) {
            setAuthentication(authentication)
        } else {
            removeAuthentication()
        }

        filterChain.doFilter(request, response)
    }

    private fun extractAuthHeader(request: HttpServletRequest): String? {
        val authHeader = request.getHeader(HttpHeaders.AUTHORIZATION)
        if (authHeader == null || authHeader.trim().isEmpty() || !authHeader.trim().startsWith(AUTH_HEADER_VALUE_PREFIX)) {
            return null
        }
        return authHeader
    }
}

fun setAuthentication(authentication: Authentication) {
    SecurityContextHolder.getContext().authentication = authentication
}

fun removeAuthentication() {
    SecurityContextHolder.clearContext()
}
