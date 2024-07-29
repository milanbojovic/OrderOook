package com.valr.orderbook.repository

import com.valr.orderbook.data.User
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

/**
 * Repository class for managing users.
 * This class provides methods to handle user data, including login and creating system users.
 *
 * @property adminUsername The username for the admin user, injected from application properties.
 * @property adminPassword The password for the admin user, injected from application properties.
 */
@Component
data class UserRepository(
    @Value("\${admin.username}") private val adminUsername: String,
    @Value("\${admin.password}") private val adminPassword: String
) {
    private val users: MutableList<User> = LinkedList()
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    /**
     * Initializes the repository by creating system users.
     * This method is called after the bean is constructed.
     */
    @PostConstruct
    fun insertData() {
        createSystemUsers(users)
    }

    /**
     * Creates system users and adds them to the user list.
     *
     * @param users The list of users to add the system users to.
     */
    private fun createSystemUsers(users: MutableList<User>) {
        users.add(User("Administrator", "Administrator", "admin@valr.com", adminUsername, passwordEncoder.encode(adminPassword)))
    }

    /**
     * Logs in a user by checking the provided username and password.
     *
     * @param username The username of the user attempting to log in.
     * @param password The password of the user attempting to log in.
     * @return An Optional containing the User if the login is successful, otherwise an empty Optional.
     */
    fun login(username: String, password: String): Optional<User> {
        return users.stream()
            .filter { u -> u.username == username && passwordEncoder.matches(password, u.password) }
            .findFirst()
    }
}