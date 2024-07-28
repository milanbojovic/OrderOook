package com.valr.orderbook.security

import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

/**
 * Filter class for processing JWT authentication in each request.
 */
@Component
class JwtRequestFilter : OncePerRequestFilter() {

    @Autowired
    private lateinit var jwtUtil: JwtUtil

    /**
     * Filters incoming requests to validate JWT tokens and set authentication context.
     *
     * @param request the HTTP request
     * @param response the HTTP response
     * @param filterChain the filter chain
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Throws(ServletException::class, IOException::class)
    override fun doFilterInternal(request: HttpServletRequest, response: HttpServletResponse, filterChain: FilterChain) {
        val authorizationHeader = request.getHeader("Authorization")
        var jwt = ""
        var username: String? = null

        // Extract JWT token and username from the Authorization header
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7)
            username = jwtUtil.extractUsername(jwt)
            logger.info("Authentication method JWT called with username $username")
            logger.info("Bearer token: $jwt")
        }

        // Validate the token and set the authentication context
        if (username != null && SecurityContextHolder.getContext().authentication == null) {
            if (jwtUtil.validateToken(jwt)) {
                val usernamePasswordAuthenticationToken = UsernamePasswordAuthenticationToken(username, null, null)
                usernamePasswordAuthenticationToken.details = WebAuthenticationDetailsSource().buildDetails(request)
                SecurityContextHolder.getContext().authentication = usernamePasswordAuthenticationToken
            }
        }
        filterChain.doFilter(request, response)
    }
}