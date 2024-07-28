package com.valr.orderbook.security

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class JwtUtilTest {
    private lateinit var jwtUtil: JwtUtil

    @BeforeEach
    fun setUp() {
        jwtUtil = JwtUtil()
    }

    @Test
    fun `generateToken should return a valid token`() {
        val username = "testuser"
        val token = jwtUtil.generateToken(username)
        assertNotNull(token)
    }

    @Test
    fun `validateToken should return true for a valid token`() {
        val username = "testuser"
        val token = jwtUtil.generateToken(username)
        assertTrue(jwtUtil.validateToken(token))
    }

    @Test
    fun `validateToken should return false for an invalid token`() {
        val token = "invalidToken"
        assertFalse(jwtUtil.validateToken(token))
    }

    @Test
    fun `getUsernameFromToken should return the correct username`() {
        val username = "testuser"
        val token = jwtUtil.generateToken(username)
        assertEquals(username, jwtUtil.extractUsername(token))
    }
}