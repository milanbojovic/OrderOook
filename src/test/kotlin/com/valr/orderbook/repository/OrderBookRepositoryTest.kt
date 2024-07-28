package com.valr.orderbook.repository

import com.valr.orderbook.data.Order
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.TestHelper.BTC_ZAR
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@SpringBootTest
class OrderBookRepositoryTest {

    private lateinit var orderBookRepository: OrderBookRepository

    @BeforeEach
    fun setUp() {
        orderBookRepository = OrderBookRepository()
        orderBookRepository.insertData()
    }

    @Test
    fun filter_by_currency_pair_returns_correct_size() {
        val result = orderBookRepository.filterOrderBookBy(BTC_ZAR)
        assertEquals(4, result.asks.size)
        assertEquals(4, result.bids.size)
    }

    @Test
    fun create_order_buy_side_no_match_adds_to_order_book() {
        val order = Order(Side.BUY, 0.5, 100, BTC_ZAR)
        val result = orderBookRepository.createOrder(order)

        assertNull(result)
        assertTrue(orderBookRepository.orderBook.bids.contains(order) == true)
    }

    @Test
    fun create_order_sell_side_no_match_adds_to_order_book() {
        val order = Order(Side.SELL, 0.5, 100, BTC_ZAR)
        val result = orderBookRepository.createOrder(order)

        assertNull(result)
        assertTrue(orderBookRepository.orderBook.asks.contains(order) == true)
    }

    @Test
    fun create_order_buy_side_full_match_order_removed() {
        val order = Order(Side.BUY, 0.5, 100, BTC_ZAR)
        val matchedOrder = Order(Side.SELL, 0.5, 100, BTC_ZAR)

        orderBookRepository.createOrder(matchedOrder)
        assertTrue(orderBookRepository.orderBook.asks.contains(matchedOrder) == true)
        val result = orderBookRepository.createOrder(order)

        assertEquals(order, result)
        assertFalse(orderBookRepository.orderBook.asks.contains(matchedOrder) == true)
    }

    @Test
    fun create_order_sell_side_full_match_order_removed() {
        val order = Order(Side.SELL, 0.5, 100, BTC_ZAR)
        val matchedOrder = Order(Side.BUY, 0.5, 100, BTC_ZAR)

        orderBookRepository.createOrder(matchedOrder)
        assertTrue(orderBookRepository.orderBook.bids.contains(matchedOrder) == true)
        val result = orderBookRepository.createOrder(order)

        assertEquals(order, result)
        assertFalse(orderBookRepository.orderBook.bids.contains(matchedOrder) == true)
    }

    @Test
    fun create_order_buy_side_partial_match_updates_quantity() {
        val order = Order(Side.BUY, 0.5, 100, BTC_ZAR)
        val matchedOrder = Order(Side.SELL, 1.0, 100, BTC_ZAR)

        orderBookRepository.createOrder(matchedOrder)
        val result = orderBookRepository.createOrder(order)

        assertEquals(order, result)
        assertEquals(0.5, matchedOrder.quantity)
        assertTrue(orderBookRepository.orderBook.asks.contains(matchedOrder) == true)
    }

    @Test
    fun create_order_sell_side_partial_match_updates_quantity() {
        val order = Order(Side.SELL, 0.5, 100, BTC_ZAR)
        val matchedOrder = Order(Side.BUY, 1.0, 100, BTC_ZAR)

        orderBookRepository.createOrder(matchedOrder)
        val result = orderBookRepository.createOrder(order)

        assertEquals(order, result)
        assertEquals(0.5, matchedOrder.quantity)
        assertTrue(orderBookRepository.orderBook.bids.contains(matchedOrder) == true)
    }

    @Test
    fun create_order_buy_side_same_price_groups_orders() {
        val order = Order(Side.BUY, 0.5, 100, BTC_ZAR)
        val existingOrder = Order(Side.BUY, 0.5, 100, BTC_ZAR)

        orderBookRepository.createOrder(existingOrder)
        val result = orderBookRepository.createOrder(order)

        assertNull(result)
        assertEquals(1.0, existingOrder.quantity)
        assertTrue(orderBookRepository.orderBook.bids.contains(existingOrder) == true)
    }

    @Test
    fun create_order_sell_side_same_price_groups_orders() {
        val order = Order(Side.SELL, 0.5, 100, BTC_ZAR)
        val existingOrder = Order(Side.SELL, 0.5, 100, BTC_ZAR)

        orderBookRepository.createOrder(existingOrder)
        val result = orderBookRepository.createOrder(order)

        assertNull(result)
        assertEquals(1.0, existingOrder.quantity)
        assertTrue(orderBookRepository.orderBook.asks.contains(existingOrder) == true)
    }
}