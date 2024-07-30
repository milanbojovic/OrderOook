package com.valr.orderbook.repository

import com.valr.orderbook.data.Order
import com.valr.orderbook.data.OrderBook
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.CurrencyPairConstants.*
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component
import java.time.Instant

/**
 * Repository class for managing the order book (in memory - easily switchable to database if needed).
 */
@Component
class OrderBookRepository {

    /**
     * The current state of the OrderBook.
     */
    var orderBook: OrderBook = OrderBook()

    /**
     * Filters the OrderBook by the given currency pair.
     *
     * @param currencyPair The currency pair to filter by.
     * @return A filtered OrderBook containing only orders for the specified currency pair.
     */
    fun filterOrderBookBy(currencyPair: String): OrderBook {
        return OrderBook(
            asks = filterOrderBookList(orderBook.asks, currencyPair),
            bids = filterOrderBookList(orderBook.bids, currencyPair),
            lastChange = orderBook.lastChange
        )
    }

    /**
     * Filters a list of orders by the given currency pair.
     *
     * @param orderList The list of orders to filter.
     * @param currencyPair The currency pair to filter by.
     * @return A list of orders filtered by the specified currency pair.
     */
    private fun filterOrderBookList(orderList: MutableList<Order>, currencyPair: String): MutableList<Order> {
        return orderList.filter { it.currencyPair == currencyPair }.toMutableList()
    }

    /**
     * Creates a new order and tries to match it with existing orders if possible.
     * Also does the necessary housekeeping activities like updating the quantity or removing the matched order
     * from the list.
     *
     * @param order The order to create.
     * @return The matched order if found, otherwise null.
     */
    fun createOrder(order: Order): Order? {
        val matchOrder = matchOppositeOrderType(order)
        return if (matchOrder != null) {
            matchedOrderExecution(order, matchOrder)
        } else {
            unmatchedOrderExecution(order)
            null
        }
    }

    /**
     * Executes logic for matched order. If the quantities are equal, the matched order is removed.
     * Otherwise, the matched order's quantity is updated and the order book is sorted to reflect possible
     * order update due to change in quantity.
     *
     * @param order the order to execute
     * @param matchedOrder the matched order
     * @return the executed order
     */
    private fun matchedOrderExecution(order: Order, matchedOrder: Order): Order {
        val quantityDiff = subQuantities(order, matchedOrder)
        if (quantityDiff == 0.0) {
            removeOrder(matchedOrder)
        } else {
            matchedOrder.quantity = quantityDiff
        }
        return order
    }

    /**
     * Executes logic for unmatched order in opposite Side.
     * It tries to match the order with the same price. If found, the quantities are added.
     * Otherwise, the order is added to the order book and the order book is sorted.
     *
     * @param order the order to execute
     */
    private fun unmatchedOrderExecution(order: Order) {
        matchSamePriceOrder(order)?.let {
            it.quantity = addQuantities(order, it)
        } ?: addToOrderBook(order)
    }

    /**
     * Subtracts the quantities of two orders.
     *
     * @param order the order to subtract from
     * @param matchedOrder the order to subtract
     * @return the difference in quantities
     */
    private fun subQuantities(order: Order, matchedOrder: Order): Double {
        return matchedOrder.quantity - order.quantity
    }

    /**
     * Adds the quantities of two orders.
     *
     * @param order the order to add to
     * @param matchedOrder the order to add
     * @return the sum of quantities
     */
    private fun addQuantities(order: Order, matchedOrder: Order): Double {
        return matchedOrder.quantity + order.quantity
    }

    /**
     * Sorts the order book by inspecting the Side of specified order.
     *
     * @param order the order to sort by
     */
    private fun sortOrderBookBy(order: Order) {
        if (order.side == Side.BUY) {
            orderBook.bids.sort()
        } else {
            orderBook.asks.sort()
        }
    }

    /**
     * Adds a new order to the order book list.
     *
     * @param newOrder the order to add
     */
    private fun addToOrderBook(newOrder: Order) {
        if (newOrder.side == Side.BUY) {
            orderBook.bids.add(newOrder)
        } else {
            orderBook.asks.add(newOrder)
        }
        sortOrderBookBy(newOrder)
    }

    /**
     * Removes an order from the corresponding collection Asks/Bids by inspecting the Side of the Order.
     *
     * @param order the order to remove
     */
    private fun removeOrder(order: Order) {
        if (order.side == Side.BUY) {
            orderBook.bids.remove(order)
        } else {
            orderBook.asks.remove(order)
        }
    }


    /**
     * Matches an order with the opposite order type.
     *
     * @param order the order to match
     * @return an optional containing the matched order if found, otherwise empty
     */
    private fun matchOppositeOrderType(order: Order): Order? {
        return if (order.side == Side.BUY) {
            orderBook.asks.firstOrNull { it.currencyPair == order.currencyPair && it.price <= order.price && it.quantity >= order.quantity }
        } else {
            return orderBook.bids.firstOrNull { it.currencyPair == order.currencyPair && it.price >= order.price && it.quantity >= order.quantity }
        }
    }

    /**
     * Matches an order with the same price.
     *
     * @param order the order to match
     * @return an optional containing the matched order if found, otherwise empty
     */
    private fun matchSamePriceOrder(order: Order): Order? {
        val orders = if (order.side == Side.BUY) orderBook.bids else orderBook.asks
        return orders.firstOrNull { it.price == order.price }
    }

    /**
     * Adds initial data to the repository on startup.
     * This is just for easier presentation purposes; for a live system, initialization would be added in unit tests.
     */
    @PostConstruct
    fun insertData() {
        val orderBook = createExampleOrderBook()
        orderBook.lastChange = Instant.now().toString()
        this.orderBook = orderBook
    }

    /**
     * Creates an example order book with initial data.
     *
     * @return an OrderBook object with initial data
     */
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