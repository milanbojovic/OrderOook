package com.valr.orderbook.service

import com.valr.orderbook.data.LimitOrderDTO
import com.valr.orderbook.data.Order
import com.valr.orderbook.data.OrderBook
import com.valr.orderbook.repository.OrderBookRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service class for managing order book operations.
 */
@Service
class OrderBookService @Autowired constructor(
    private val orderBookRepository: OrderBookRepository
) {

    /**
     * Retrieves the order book for a given currency pair.
     *
     * @param currencyPair the currency pair to filter the order book by
     * @return the filtered order book
     */
    fun getOrderBookBy(currencyPair: String): OrderBook {
        return orderBookRepository.filterOrderBookBy(currencyPair.uppercase())
    }

    /**
     * Creates a limit order based on the given limit order DTO.
     *
     * @param limitOrderDTO the limit order data transfer object
     * @return the created order
     */
    fun createLimitOrder(limitOrderDTO: LimitOrderDTO): Order? {
        return orderBookRepository.createOrder(Order(limitOrderDTO))
    }
}