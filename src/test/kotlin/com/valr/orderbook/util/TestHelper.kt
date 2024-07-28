package com.valr.orderbook.util

import com.valr.orderbook.data.Order
import com.valr.orderbook.data.OrderBook
import com.valr.orderbook.data.enum.Side

object TestHelper {
    const val BTC_EUR = "BTCEUR"
    const val BTC_ZAR = "BTCZAR"
    const val BTC_USD = "BTCUSD"
    const val LTC_USD = "LTCUSD"

    fun createOrderBook(): OrderBook {
        return OrderBook(
            bids = createOrdersList(Side.BUY),
            asks = createOrdersList(Side.SELL)
        )
    }

    fun createOrdersList(side: Side): MutableList<Order> {
        return mutableListOf(
            Order(side, 1.0, 1, BTC_ZAR),
            Order(side, 2.0, 2, BTC_EUR),
            Order(side, 3.0, 3, BTC_ZAR)
        )
    }

    fun createOrder(side: Side, quantity: Double, price: Int, currencyPair: String): Order {
        return Order(
            side = side,
            quantity = quantity,
            price = price,
            currencyPair = currencyPair
        )
    }
}