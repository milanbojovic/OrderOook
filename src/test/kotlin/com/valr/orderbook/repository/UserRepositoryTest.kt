package com.valr.orderbook.repository

import com.valr.orderbook.data.User
import com.valr.orderbook.service.UserService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import java.util.*
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as whenever

class UserRepositoryTest {

    @Mock
    private lateinit var userRepository: UserRepository

    @InjectMocks
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testLogin() {
        val user = User()
        whenever(userRepository.login("username", "password")).thenReturn(Optional.of(user))

        val result = userService.login("username", "password")
        assertTrue(result.isPresent)
    }
}