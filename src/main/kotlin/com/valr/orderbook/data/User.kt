package com.valr.orderbook.data

data class User(
    var firstName: String = "",
    var lastName: String = "",
    var email: String = "",
    var username: String = "",
    var password: String = ""
) {
    override fun toString(): String {
        return "User(firstName='$firstName', lastName='$lastName', email='$email', username='*****', password='*****')"
    }
}