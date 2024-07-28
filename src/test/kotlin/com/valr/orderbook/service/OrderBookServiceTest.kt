package com.valr.orderbook.service

import com.valr.orderbook.data.LimitOrderDTO
import com.valr.orderbook.data.Order
import com.valr.orderbook.data.OrderBook
import com.valr.orderbook.repository.OrderBookRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.MockitoAnnotations
import kotlin.test.assertEquals
import org.mockito.Mockito.`when` as whenever

class OrderBookServiceTest {

    @Mock
    private lateinit var orderBookRepository: OrderBookRepository

    @InjectMocks
    private lateinit var orderBookService: OrderBookService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
    }

    @Test
    fun testGetOrderBookBy() {
        val orderBook = OrderBook()
        whenever(orderBookRepository.filterOrderBookBy("BTCUSD")).thenReturn(orderBook)

        val result = orderBookService.getOrderBookBy("BTCUSD")
        assertEquals(orderBook, result)
    }

    @Test
    fun testCreateLimitOrder() {
        val limitOrderDTO = LimitOrderDTO()
        val order = Order(limitOrderDTO)
        whenever(orderBookRepository.createOrder(order)).thenReturn(order)

        val result = orderBookService.createLimitOrder(limitOrderDTO)
        assertEquals(order, result)
    }
}