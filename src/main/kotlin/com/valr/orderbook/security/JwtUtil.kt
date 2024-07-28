package com.valr.orderbook.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Component
import java.security.Key
import java.util.*

/**
 * Utility class for handling JWT operations such as generating, validating, and extracting information from tokens.
 */
@Component
class JwtUtil {
    companion object {
        private val SECRET_KEY: Key = Keys.secretKeyFor(SignatureAlgorithm.HS512)
        private const val EXPIRATION_TIME: Long = 1800_000 // 30 minutes
    }

    /**
     * Generates a JWT token for the given username.
     *
     * @param username the username for which the token is generated
     * @return the generated JWT token
     */
    fun generateToken(username: String): String {
        return Jwts.builder()
            .setSubject(username)
            .setExpiration(Date(System.currentTimeMillis() + EXPIRATION_TIME))
            .signWith(SECRET_KEY)
            .compact()
    }

    /**
     * Validates the given JWT token.
     *
     * @param token the JWT token to validate
     * @return true if the token is valid, false otherwise
     */
    fun validateToken(token: String): Boolean {
        return try {
            Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Extracts the username from the given JWT token.
     *
     * @param token the JWT token from which the username is extracted
     * @return the extracted username
     */
    fun extractUsername(token: String): String {
        return Jwts.parserBuilder().setSigningKey(SECRET_KEY).build().parseClaimsJws(token).body.subject
    }
}