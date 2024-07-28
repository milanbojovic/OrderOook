package com.valr.orderbook.data

data class OrderBook(
        var asks: MutableList<Order> = mutableListOf(),
        var bids: MutableList<Order> = mutableListOf(),
        var lastChange: String = ""
)