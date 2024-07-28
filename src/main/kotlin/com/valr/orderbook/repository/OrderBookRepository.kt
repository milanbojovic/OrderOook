package com.valr.orderbook.repository

import com.valr.orderbook.data.Order
import com.valr.orderbook.data.OrderBook
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.CurrencyPairConstants.*
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class OrderBookRepository {

    var orderBook: OrderBook = OrderBook()

    fun filterOrderBookBy(currencyPair: String): OrderBook {
        return OrderBook(
            asks = filterOrderBookList(orderBook.asks, currencyPair),
            bids = filterOrderBookList(orderBook.bids, currencyPair),
            lastChange = orderBook.lastChange
        )
    }

    private fun filterOrderBookList(orderList: MutableList<Order>, currencyPair: String): MutableList<Order> {
        return orderList.filter { it.currencyPair == currencyPair }.toMutableList()
    }

    fun createOrder(order: Order): Order? {
        val matchOrder = matchOppositeOrderType(order)
        return if (matchOrder != null) {
            matchedOrderExecution(order, matchOrder)
        } else {
            unmatchedOrderExecution(order)
            null
        }
    }

    private fun matchedOrderExecution(order: Order, matchedOrder: Order): Order {
        val quantityDiff = subQuantities(order, matchedOrder)
        if (quantityDiff == 0.0) {
            removeOrder(matchedOrder)
        } else {
            matchedOrder.quantity = quantityDiff
        }
        return order
    }

    private fun unmatchedOrderExecution(order: Order) {
        matchSamePriceOrder(order)?.let {
            it.quantity = addQuantities(order, it)
        } ?: addToOrderBook(order)
    }

    private fun subQuantities(order: Order, matchedOrder: Order): Double {
        return matchedOrder.quantity - order.quantity
    }

    private fun addQuantities(order: Order, matchedOrder: Order): Double {
        return matchedOrder.quantity + order.quantity
    }

    private fun sortOrderBookBy(order: Order) {
        if (order.side == Side.BUY) {
            orderBook.bids.sorted()
        } else {
            orderBook.asks.sorted()
        }
    }

    private fun addToOrderBook(newOrder: Order) {
        if (newOrder.side == Side.BUY) {
            orderBook.bids.add(newOrder)
        } else {
            orderBook.asks.add(newOrder)
        }
        sortOrderBookBy(newOrder)
    }

    private fun removeOrder(order: Order) {
        if (order.side == Side.BUY) {
            orderBook.bids.remove(order)
        } else {
            orderBook.asks.remove(order)
        }
    }

    private fun matchOppositeOrderType(order: Order): Order? {
        return if (order.side == Side.BUY) {
            matchBuyOrdersByPriceAndQuantity(order)
        } else {
            matchSellOrdersByPriceAndQuantity(order)
        }
    }

    private fun matchBuyOrdersByPriceAndQuantity(order: Order): Order? {
        return orderBook.asks.firstOrNull { it.price <= order.price && it.quantity >= order.quantity }
    }

    private fun matchSellOrdersByPriceAndQuantity(order: Order): Order? {
        return orderBook.bids.firstOrNull { it.price >= order.price && it.quantity >= order.quantity }
    }

    private fun matchSamePriceOrder(order: Order): Order? {
        val orders = if (order.side == Side.BUY) orderBook.bids else orderBook.asks
        return orders.firstOrNull { it.price == order.price }
    }

    @PostConstruct
    fun insertData() {
        val orderBook = createExampleOrderBook()
        orderBook.lastChange = Instant.now().toString()
        this.orderBook = orderBook
    }

    private fun createExampleOrderBook(): OrderBook {
        return OrderBook(
            asks = mutableListOf(
                Order(Side.SELL, 0.90038334, 1186331, BTC_EUR),
                Order(Side.SELL, 0.02350766, 1202530, BTC_EUR),
                Order(Side.SELL, 0.00100004, 1203000, BTC_ZAR),
                Order(Side.SELL, 0.02352094, 1205649, BTC_ZAR),
                Order(Side.SELL, 0.552, 1205653, BTC_ZAR),
                Order(Side.SELL, 0.0008979, 1205748, ETH_USD),
                Order(Side.SELL, 0.001, 1207000, BTC_ZAR)
            ),
            bids = mutableListOf(
                Order(Side.BUY, 0.016, 1204994, BTC_ZAR),
                Order(Side.BUY, 0.002036, 1204993, BTC_ZAR),
                Order(Side.BUY, 0.18443981, 1204991, ETH_USD),
                Order(Side.BUY, 0.00008142, 1204811, BTC_EUR),
                Order(Side.BUY, 0.02354031, 1204657, BTC_EUR),
                Order(Side.BUY, 0.11498758, 1204532, BTC_ZAR),
                Order(Side.BUY, 0.05, 1164656, BTC_ZAR)
            )
        )
    }
}