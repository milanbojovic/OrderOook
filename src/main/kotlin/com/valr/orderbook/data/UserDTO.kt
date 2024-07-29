package com.valr.orderbook.data

import com.fasterxml.jackson.annotation.JsonProperty
import javax.validation.constraints.NotNull

data class UserDTO(
    @JsonProperty("username") @field:NotNull val username: String,
    @JsonProperty("password") @field:NotNull val password: String
)