package com.valr.orderbook.service

import com.valr.orderbook.data.User
import com.valr.orderbook.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.*

/**
 * Service class for managing user-related operations.
 */
@Service
class UserService @Autowired constructor(
    private val userRepository: UserRepository
) {

    /**
     * Authenticates a user by username and password.
     *
     * @param username the username of the user
     * @param password the password of the user
     * @return an Optional containing the authenticated user if found, otherwise empty
     */
    fun login(username: String, password: String): Optional<User> {
        return userRepository.login(username, password)
    }
}