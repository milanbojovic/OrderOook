package com.valr.orderbook.repository

import com.valr.orderbook.data.Trade
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.TestHelper.BTC_USD
import com.valr.orderbook.util.TestHelper.BTC_ZAR
import com.valr.orderbook.util.TestHelper.LTC_USD
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest
class TradeHistoryRepositoryTest {

    private lateinit var tradeHistoryRepository: TradeHistoryRepository

    @BeforeEach
    fun setUp() {
        tradeHistoryRepository = TradeHistoryRepository()
        tradeHistoryRepository.insertData()
        addAdditionalTestData()
    }

    private fun addAdditionalTestData() {
        val additionalTrades = listOf(
            Trade(tradeHistoryRepository.getNextAvailableId(), 1234567, 0.001, BTC_ZAR, "2024-12-01T10:00:00.000Z", Side.BUY, 50000.0),
            Trade(tradeHistoryRepository.getNextAvailableId(), 1234568, 0.002, BTC_ZAR, "2024-12-01T10:05:00.000Z", Side.SELL, 51000.0),
            Trade(tradeHistoryRepository.getNextAvailableId(), 1234569, 0.003, BTC_USD, "2024-12-01T10:10:00.000Z", Side.BUY, 60000.0),
            Trade(tradeHistoryRepository.getNextAvailableId(), 1234570, 0.004, BTC_USD, "2024-12-01T10:15:00.000Z", Side.SELL, 61000.0)
        )
        additionalTrades.forEach { tradeHistoryRepository.addTrade(it) }
    }

    @Test
    fun filter_trade_history_by_currency_pair_returns_correct_trades() {
        val result = tradeHistoryRepository.filterTradeHistoryBy(BTC_ZAR, 0, 10)
        assertEquals(8, result.trades.size)
    }

    @Test
    fun filter_trade_history_by_currency_pair_with_skip_and_limit() {
        val result = tradeHistoryRepository.filterTradeHistoryBy(BTC_ZAR, 2, 3)
        assertEquals(3, result.trades.size)
        assertEquals(7, result.trades[0].id)
    }

    @Test
    fun filter_trade_history_by_currency_pair_with_no_trades() {
        val result = tradeHistoryRepository.filterTradeHistoryBy(LTC_USD, 0, 10)
        assertTrue(result.trades.isEmpty())
    }

    @Test
    fun filter_trade_history_by_currency_pair_with_skip_exceeding_trades() {
        val result = tradeHistoryRepository.filterTradeHistoryBy(BTC_ZAR, 10, 5)
        assertTrue(result.trades.isEmpty())
    }

    @Test
    fun filter_trade_history_by_currency_pair_with_limit_exceeding_trades() {
        val result = tradeHistoryRepository.filterTradeHistoryBy(BTC_ZAR, 0, 20)
        assertEquals(8, result.trades.size)
    }
}