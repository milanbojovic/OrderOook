package com.valr.orderbook.data

data class TradeHistory(
    var trades: MutableList<Trade> = mutableListOf()
)