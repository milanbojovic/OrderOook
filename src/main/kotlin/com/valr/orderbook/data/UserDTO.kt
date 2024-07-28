package com.valr.orderbook.data

import javax.validation.constraints.NotNull

data class UserDTO(
    @field:NotNull val username: String,
    @field:NotNull val password: String
)