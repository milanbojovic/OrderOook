package com.valr.orderbook.repository

import com.valr.orderbook.data.Trade
import com.valr.orderbook.data.TradeHistory
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.CurrencyPairConstants.*
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

/**
 * Repository class for managing trade history.
 * This class provides methods to retrieve trade history data.
 */
@Component
class TradeHistoryRepository {

    var tradeHistory: TradeHistory = TradeHistory()

    /**
     * Retrieves the trade history for a given currency pair.
     *
     * @param currencyPair The currency pair to retrieve trade history for.
     * @param skip The number of records to skip.
     * @param limit The maximum number of records to return.
     * @return A TradeHistory object containing the trade history for the specified currency pair.
     */
    fun filterTradeHistoryBy(currencyPair: String, skip: Int, limit: Int): TradeHistory {
        return TradeHistory(
            trades = tradeHistory.trades
                    .filter { it.currencyPair == currencyPair }
                    .drop(skip)
                    .take(limit)
                    .toMutableList()
        )
    }

    /**
     * Adds a new trade to the trade history.
     *
     * @param tradeHistory The TradeHistory object containing the new trade to add.
     */
    fun addTrade(trade: Trade) {
        tradeHistory.trades.add(trade)
    }

    /**
     * Clears the trade history for a given currency pair.
     *
     * @param currencyPair The currency pair to clear trade history for.
     */
    fun getNextAvailableId(): Int {
        return tradeHistory.trades.maxOfOrNull { it.id }?.plus(1) ?: 0
    }

    /**
     * Adds initial data to the repository on startup.
     * This is just for easier presentation purposes; for a live system, initialization would be added in unit tests.
     */
    @PostConstruct
    fun insertData() {
        createExampleTradesList(tradeHistory)
    }

    /**
     * Creates an example trades  with initial data and adds them to tradeHistory.
     */
    private fun createExampleTradesList(tradeHistory: TradeHistory) {
        val trades = mutableListOf<Trade>()
        tradeHistory.trades = trades
        trades.add(Trade(getNextAvailableId(), 1199677, 0.00213752, BTC_EUR, "2024-07-11T08:50:12.453Z", Side.SELL, 2564.33358104))
        trades.add(Trade(getNextAvailableId(), 1200677, 0.03225700, BTC_USD, "2024-08-10T09:22:15.363Z", Side.SELL, 38730.237989))
        trades.add(Trade(getNextAvailableId(), 1230650, 0.00456120, ETH_ZAR, "2024-09-15T18:32:16.363Z", Side.SELL, 5613.24078))
        trades.add(Trade(getNextAvailableId(), 1358400, 0.75689132, ETH_EUR, "2024-10-17T14:22:18.433Z", Side.SELL, 1028161.169088))
        trades.add(Trade(getNextAvailableId(), 1005522, 2.56879135, ETH_USD, "2024-11-19T03:52:17.413Z", Side.SELL, 2582976.2158347))
        trades.add(Trade(getNextAvailableId(), 1015459, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
        trades.add(Trade(getNextAvailableId(), 5168975, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
        trades.add(Trade(getNextAvailableId(), 2159877, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
        trades.add(Trade(getNextAvailableId(), 1111115, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
        trades.add(Trade(getNextAvailableId(), 2222222, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
        trades.add(Trade(getNextAvailableId(), 4567895, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.BUY, 570680.8748647))
    }
}