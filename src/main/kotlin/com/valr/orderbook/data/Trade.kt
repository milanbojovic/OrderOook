package com.valr.orderbook.data

import com.valr.orderbook.data.enum.Side
import java.time.Instant

data class Trade(
        val id: Int,
        val price: Int,
        val quantity: Double,
        val currencyPair: String,
        val tradedAt: String,
        val takerSide: Side,
        val quoteVolume: Double
) {
    constructor(order: Order, id: Int) : this(
        id = id,
        price = order.price,
        quantity = order.quantity,
        currencyPair = order.currencyPair,
        tradedAt = Instant.now().toString(),
        takerSide = order.side,
        quoteVolume = order.price * order.quantity
    )
}