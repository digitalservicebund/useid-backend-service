package de.bund.digitalservice.useid.apikeys

import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpHeaders
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.web.filter.OncePerRequestFilter

private const val AUTH_HEADER_VALUE_PREFIX = "Bearer "

class ApiKeyAuthenticationFilter(private val authenticationManager: AuthenticationManager) :
    OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        val authHeader: String? = extractAuthHeader(request)
        val apiKey = authHeader?.substring(AUTH_HEADER_VALUE_PREFIX.length)
        val authentication = apiKey?.let { authenticationManager.authenticate(ApiKeyAuthenticationToken(apiKey)) }

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
