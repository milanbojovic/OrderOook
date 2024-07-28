package com.valr.orderbook.data

import com.valr.orderbook.data.enum.Side

data class Order(
    var side: Side,
    var quantity: Double,
    var price: Int,
    var currencyPair: String
) : Comparable<Order> {

    constructor(orderDTO: LimitOrderDTO) : this(orderDTO.side, orderDTO.quantity, orderDTO.price, orderDTO.currencyPair)

    override fun compareTo(other: Order): Int {
        return if (side == Side.SELL) {
            price.compareTo(other.price)
        } else {
            other.price.compareTo(price)
        }
    }
}