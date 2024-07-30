package com.valr.orderbook.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.valr.orderbook.data.LimitOrderDTO
import com.valr.orderbook.data.TradeHistory
import com.valr.orderbook.data.UserDTO
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.service.OrderBookService
import com.valr.orderbook.service.TradeHistoryService
import com.valr.orderbook.util.CurrencyPairConstants
import com.valr.orderbook.util.TestHelper.BTC_ZAR
import com.valr.orderbook.util.TestHelper.ETH_USD
import io.vertx.core.Vertx
import io.vertx.core.http.HttpClientResponse
import io.vertx.core.http.HttpMethod
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.MockitoAnnotations
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.event.annotation.BeforeTestClass

private const val CONTENT_TYPE = "content-type"
private const val HOST = "localhost"
private const val APPLICATION_JSON = "application/json"

private const val ADMIN = "admin"

@SpringBootTest
@ExtendWith(VertxExtension::class)
class WebControllerTest() {
    @Value("\${vertex.server.port}")
    private var vertXServerPort: Int = 0

    @Autowired
    private lateinit var webVerticle: WebVerticle

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var tradeHistoryService: TradeHistoryService

    @Autowired
    lateinit var orderBookService: OrderBookService


    @BeforeTestClass
    fun deployVerticle(vertx: Vertx, testContext: VertxTestContext) {
        MockitoAnnotations.openMocks(this)
        objectMapper = ObjectMapper()
        vertx.deployVerticle(webVerticle, testContext.succeedingThenComplete())
    }

    @Test
    fun login_with_valid_credentials_returns_token(vertx: Vertx, testContext: VertxTestContext) {
        val userDTO = UserDTO(ADMIN, ADMIN)
        val json = objectMapper.writeValueAsString(userDTO)

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/user/login")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Bearer")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun login_with_invalid_credentials_returns_unauthorized(vertx: Vertx, testContext: VertxTestContext) {
        val userDTO = UserDTO("admin1", "admin1")
        val json = objectMapper.writeValueAsString(userDTO)

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/user/login")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid login request. Invalid username or password.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun login_with_null_username_returns_bad_request(vertx: Vertx, testContext: VertxTestContext) {
        val userDTO = UserDTO(null.toString(), ADMIN)
        val json = objectMapper.writeValueAsString(userDTO)

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/user/login")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid login request. Invalid username or password.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun login_with_null_pass_returns_bad_request(vertx: Vertx, testContext: VertxTestContext) {
        val userDTO = UserDTO(ADMIN, null.toString())
        val json = objectMapper.writeValueAsString(userDTO)

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/user/login")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid login request. Invalid username or password.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_orderbook_with_valid_currency_pair_returns_orderbook(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/ETHUSD/orderbook")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains(ETH_USD)
                    assertThat(buffer.toString()).contains("\"quantity\":8.979E-4,\"price\":1205748")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_orderbook_with_invalid_currency_pair_returns_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/ETHUSD1/orderbook")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains(
                        "Invalid currency pair. Please provide a 6 character currency " +
                                "pair - valid example: BTCZAR | btczar."
                    )
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_orderbook_with_special_character_in_currency_pair_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTC@AR/orderbook")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains(
                        "Invalid currency pair. Please provide a 6 character currency " +
                                "pair - valid example: BTCZAR | btczar."
                    )
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun create_limit_order_with_valid_data_full_match_returns_success(vertx: Vertx, testContext: VertxTestContext) {
        val limitOrder = LimitOrderDTO(Side.BUY, 0.00100004, 1203000, CurrencyPairConstants.BTC_ZAR)
        val json = objectMapper.writeValueAsString(limitOrder)
        val client = vertx.createHttpClient()

        val initialTradeHistorySize = tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
        val initialBidsSize = orderBookService.getOrderBookBy(BTC_ZAR).asks.size

        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/order/limit")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertEquals("\"Limit order created successfully.\"", buffer.toString())
                    assertEquals(
                        initialTradeHistorySize + 1,
                        tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
                    )
                    assertEquals(initialBidsSize - 1, orderBookService.getOrderBookBy(BTC_ZAR).asks.size)
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun create_limit_order_with_valid_data_partial_match_returns_success(vertx: Vertx, testContext: VertxTestContext) {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.10498758, 1204532, BTC_ZAR)
        val json = objectMapper.writeValueAsString(limitOrder)
        val client = vertx.createHttpClient()

        val initialTradeHistorySize = tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
        val initialBidsSize = orderBookService.getOrderBookBy(BTC_ZAR).bids.size

        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/order/limit")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertEquals("\"Limit order created successfully.\"", buffer.toString())
                    assertEquals(
                        initialTradeHistorySize + 1,
                        tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
                    )
                    assertEquals(initialBidsSize, orderBookService.getOrderBookBy(BTC_ZAR).bids.size)
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun create_limit_order_with_valid_data_but_no_matched_order_returns_success(
        vertx: Vertx,
        testContext: VertxTestContext
    ) {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.518, 123400, BTC_ZAR)
        val json = objectMapper.writeValueAsString(limitOrder)

        val initialTradeHistorySize = tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
        val initialAsksSize = orderBookService.getOrderBookBy(BTC_ZAR).asks.size

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/order/limit")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertEquals("\"Limit order created successfully.\"", buffer.toString())
                    assertEquals(
                        initialTradeHistorySize,
                        tradeHistoryService.getTradeHistoryBy(BTC_ZAR, 0, 10).trades.size
                    )
                    assertEquals(initialAsksSize + 1, orderBookService.getOrderBookBy(BTC_ZAR).asks.size)
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun create_limit_order_with_invalid_data_returns_bad_request(vertx: Vertx, testContext: VertxTestContext) {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.0, 0, BTC_ZAR)
        val json = objectMapper.writeValueAsString(limitOrder)

        val client = vertx.createHttpClient()
        client.request(HttpMethod.POST, vertXServerPort, HOST, "/api/order/limit")
            .compose { req ->
                req.putHeader(CONTENT_TYPE, APPLICATION_JSON)
                req.send(json).compose(HttpClientResponse::body)
            }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid limitOrder. Please provide a 6 character currency pair - valid example: BTCZAR | btczar")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_withhout_limit_and_skip_parames_calls_with_defaults(
        vertx: Vertx,
        testContext: VertxTestContext
    ) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTCEUR/trades")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    val tradeHistory = objectMapper.readValue(buffer.toString(), TradeHistory::class.java)
                    assertEquals(1, tradeHistory.trades.size)
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_with_valid_parameters_returns_tradehistory(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTCEUR/trades?skip=5&limit=17")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    val tradeHistory = objectMapper.readValue(buffer.toString(), TradeHistory::class.java)
                    assertEquals(0, tradeHistory.trades.size)
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_with_invalid_currency_pair_returns_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTC@AR/trades?skip=0&limit=10")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid currency pair or query parameters. Query parameters 'skip' and 'limit' must be non-negative.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_with_negative_skip_returns_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTCZAR/trades?skip=-1&limit=10")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid currency pair or query parameters. Query parameters " +
                            "'skip' and 'limit' must be non-negative.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_with_negative_limit_returns_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTCZAR/trades?skip=0&limit=-1")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid currency pair or query parameters. Query parameters " +
                            "'skip' and 'limit' must be non-negative.")
                    testContext.completeNow()
                }
            })
    }

    @Test
    fun get_tradehistory_with_limit_exceeding_max_returns_error(vertx: Vertx, testContext: VertxTestContext) {
        val client = vertx.createHttpClient()
        client.request(HttpMethod.GET, vertXServerPort, HOST, "/api/BTCZAR/trades?skip=0&limit=101")
            .compose { req -> req.send().compose(HttpClientResponse::body) }
            .onComplete(testContext.succeeding { buffer ->
                testContext.verify {
                    assertThat(buffer.toString()).contains("Invalid currency pair or query parameters. Query parameters" +
                            " 'skip' and 'limit' must be non-negative.")
                    testContext.completeNow()
                }
            })
    }
}