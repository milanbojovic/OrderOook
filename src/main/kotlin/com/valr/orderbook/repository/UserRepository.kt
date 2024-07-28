package com.valr.orderbook.repository

import com.valr.orderbook.data.User
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Component
import java.util.*

@Component
data class UserRepository(
    @Value("\${admin.username}") private val adminUsername: String,
    @Value("\${admin.password}") private val adminPassword: String
) {
    private val users: MutableList<User> = LinkedList()
    private val passwordEncoder: BCryptPasswordEncoder = BCryptPasswordEncoder()

    @PostConstruct
    fun insertData() {
        createSystemUsers(users)
    }

    private fun createSystemUsers(users: MutableList<User>) {
        users.add(User("Administrator", "Administrator", "admin@valr.com", adminUsername, passwordEncoder.encode(adminPassword)))
    }

    fun login(username: String, password: String): Optional<User> {
        return users.stream()
            .filter { u -> u.username == username && passwordEncoder.matches(password, u.password) }
            .findFirst()
    }
}