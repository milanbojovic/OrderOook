package com.valr.orderbook.repository

import com.valr.orderbook.data.Trade
import com.valr.orderbook.data.TradeHistory
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.util.CurrencyPairConstants.*
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Component

@Component
class TradeHistoryRepository {

    var tradeHistory: TradeHistory = TradeHistory()

    fun filterTradeHistoryBy(currencyPair: String, skip: Int, limit: Int): TradeHistory {
        return TradeHistory(
            trades = tradeHistory.trades
                    .filter { it.currencyPair == currencyPair }
                    .drop(skip)
                    .take(limit)
                    .toMutableList()
        )
    }

    fun addTrade(trade: Trade) {
        tradeHistory.trades.add(trade)
    }

    fun getNextAvailableId(): Int {
        return tradeHistory.trades.maxOfOrNull { it.id }?.plus(1) ?: 0
    }

    @PostConstruct
    fun insertData() {
        createExampleTradesList(tradeHistory)
    }

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
        trades.add(Trade(getNextAvailableId(), 4567895, 0.56879135, BTC_ZAR, "2022-10-11T13:44:24.571Z", Side.SELL, 570680.8748647))
    }
}