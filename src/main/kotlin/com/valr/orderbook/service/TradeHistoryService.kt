package com.valr.orderbook.service

import com.valr.orderbook.data.Order
import com.valr.orderbook.data.Trade
import com.valr.orderbook.data.TradeHistory
import com.valr.orderbook.repository.TradeHistoryRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

/**
 * Service class for managing trade history operations.
 */
@Service
class TradeHistoryService @Autowired constructor(
    private val tradeHistoryRepository: TradeHistoryRepository
) {

    /**
     * Adds a trade order to the trade history.
     *
     * @param order the order to be added as a trade
     */
    fun addTradeOrder(order: Order) {
        tradeHistoryRepository.addTrade(Trade(order, tradeHistoryRepository.getNextAvailableId()))
    }

    /**
     * Retrieves the trade history for a given currency pair with optional filtering.
     *
     * @param currencyPair the currency pair to filter the trade history by
     * @param skipSize the number of records to skip
     * @param limitSize the maximum number of records to return
     * @return the filtered trade history
     */
    fun getTradeHistoryBy(currencyPair: String, skipSize: Int, limitSize: Int): TradeHistory {
        return tradeHistoryRepository.filterTradeHistoryBy(currencyPair.uppercase(), skipSize, limitSize)
    }
}