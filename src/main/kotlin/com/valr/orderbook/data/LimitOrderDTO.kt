package com.valr.orderbook.data

import com.valr.orderbook.data.enum.Side

data class LimitOrderDTO(
        val side: Side = Side.BUY, // Provide a default value for Side
        val quantity: Double = 0.0,
        val price: Int = 0,
        val currencyPair: String = ""
)