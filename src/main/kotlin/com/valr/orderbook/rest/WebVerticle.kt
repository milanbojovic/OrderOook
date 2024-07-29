package com.valr.orderbook.rest

import com.valr.orderbook.data.*
import com.valr.orderbook.security.JwtUtil
import com.valr.orderbook.service.OrderBookService
import com.valr.orderbook.service.TradeHistoryService
import com.valr.orderbook.service.UserService
import io.vertx.core.AbstractVerticle
import io.vertx.core.Handler
import io.vertx.core.Promise
import io.vertx.core.Vertx
import io.vertx.core.http.HttpServerOptions
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.json.Json
import io.vertx.ext.web.Router
import io.vertx.ext.web.RoutingContext
import io.vertx.ext.web.handler.BodyHandler
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller

/**
 * WebVerticle is a Vert.x verticle that handles HTTP requests for the OrderBook application.
 * It is annotated with @Controller to be managed by Spring and uses dependency injection
 * to obtain instances of various services.
 *
 * @param orderBookService Service for handling order book operations.
 * @param tradeHistoryService Service for handling trade history operations.
 * @param userService Service for handling user operations.
 * @param jwtUtil Utility for handling JWT operations.
 */
@Controller
class WebVerticle @Autowired constructor(
    private val orderBookService: OrderBookService,
    private val tradeHistoryService: TradeHistoryService,
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) : AbstractVerticle() {

    companion object {
        const val CURRENCY_PAIR_PATTERN = "[A-Za-z]{6}"
        const val CURRENCY_PAIR_VALIDATION_ERROR =
            "Invalid currency pair. Please provide a 6 character currency pair - valid example: BTCZAR | btczar."
    }

    @Value("\${vertex.server.port}")
    private var vertXServerPort: Int = 0

    private val logger = LoggerFactory.getLogger(WebVerticle::class.java)

    /**
     * Starts the Vert.x verticle and creates an HTTP server.
     *
     * @param startPromise  is an object used to handle the result of an asynchronous operation in Vert.x.
     */
    override fun start(startPromise: Promise<Void>) {
        super.start()

        logger.info("Verticle Main started")
        val vertx = Vertx.vertx()
        vertx.createHttpServer(HttpServerOptions())
            .requestHandler(createHttpRouter(vertx))
            .listen(vertXServerPort) { asyncResult ->
                if (asyncResult.succeeded()) {
                    val port = asyncResult.result().actualPort()
                    logger.info("Vert.x HTTP server started on port $port")
                    startPromise.complete()
                } else {
                    logger.error("Failed to start Vert.x HTTP server", asyncResult.cause())
                    startPromise.fail(asyncResult.cause())
                }
            }
    }

    /**
     * Creates an HTTP router for handling various API endpoints.
     *
     * @param vertx The Vert.x instance.
     * @return A handler for HTTP server requests.
     */
    private fun createHttpRouter(vertx: Vertx): Handler<HttpServerRequest>? {
        val router = Router.router(vertx)

        router.post("/api/user/login").handler(BodyHandler.create()).handler { ctxt ->
            val userDto = ctxt.body().asJsonObject().mapTo(UserDTO::class.java)
            val optUser = userService.login(userDto.username, userDto.password)
            if (optUser.isEmpty) {
                ctxt.response()
                    .setStatusCode(401)
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(MyError(-24, "Invalid login request. Invalid username or password.")))
            } else {
                val existingUser = optUser.get()
                val response = mapOf("Bearer" to jwtUtil.generateToken(existingUser.username))
                ctxt.response()
                    .putHeader("content-type", "application/json")
                    .end(Json.encode(response))
            }
        }

        router.get("/api/:currencyPair/orderbook").handler { handler: RoutingContext ->
            val currencyPair = handler.pathParam("currencyPair")
            val pattern = Regex(CURRENCY_PAIR_PATTERN)
            if (!pattern.matches(currencyPair)) {
                handler.response().end(
                    ResponseEntity.badRequest().body(MyError(-21, CURRENCY_PAIR_VALIDATION_ERROR)).toString()
                )
            } else {
                responseAsJson(handler, orderBookService.getOrderBookBy(currencyPair))
            }
        }

        router.post("/api/order/limit").handler(BodyHandler.create()).handler { handler ->
            val limitOrder = handler.body().asJsonObject().mapTo(LimitOrderDTO::class.java)
            if (!limitOrder.currencyPair.matches(Regex(CURRENCY_PAIR_PATTERN)) || limitOrder.quantity <= 0 || limitOrder.price <= 0) {
                handler.response().end(
                    ResponseEntity.badRequest().body(MyError(-23, """
                Invalid limitOrder. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.
                Quantity and price must be greater than 0.
                Side must be either 'BUY' or 'SELL'.""".trimIndent())).toString()
                )
            } else {
                responseAsJson(handler, orderBookService.createLimitOrder(limitOrder).also { executedOrder ->
                    if (executedOrder != null) tradeHistoryService.addTradeOrder(executedOrder)
                })
            }
        }

        router.get("/api/:currencyPair/trades").handler { handler ->
            val currencyPair = handler.pathParam("currencyPair")
            val skip = handler.queryParam("skip").getOrElse(0) { "0" }.toIntOrNull() ?: 0
            val limit = handler.queryParam("limit").getOrElse(0) { "10" }.toIntOrNull() ?: 10
            val pattern = Regex(CURRENCY_PAIR_PATTERN)
            if (!pattern.matches(currencyPair) || skip < 0 || limit < 0 || limit > 100) {
                handler.response().end(
                    ResponseEntity.badRequest().body(MyError(-22, "Invalid currency pair or query parameters. " +
                            "Query parameters 'skip' and 'limit' must be non-negative. " +
                            "Currency pair should be 6 character long - valid example: BTCZAR | btczar")).toString()
                )
            } else {
                responseAsJson(handler, tradeHistoryService.getTradeHistoryBy(currencyPair, skip, limit))
            }
        }

        return router
    }

    /**
     * Sends a JSON response for an order creation.
     *
     * @param handler The routing context.
     * @param orderBook The order book object.
     */
    private fun responseAsJson(handler: RoutingContext, orderBook: Order?) {
        handler.response()
            .putHeader("content-type", "application/json")
            .end(Json.encode(ResponseEntity.ok().body("Limit order created successfully.").body))
    }

    /**
     * Sends a JSON response for an order book.
     *
     * @param handler The routing context.
     * @param orderBook The order book object.
     */
    private fun responseAsJson(handler: RoutingContext, orderBook: OrderBook) {
        handler.response()
            .putHeader("content-type", "application/json")
            .end(Json.encode(ResponseEntity.ok().body(orderBook).body))
    }

    /**
     * Sends a JSON response for trade history.
     *
     * @param handler The routing context.
     * @param tradeHistory The trade history object.
     */
    private fun responseAsJson(handler: RoutingContext, tradeHistory: TradeHistory) {
        handler.response()
            .putHeader("content-type", "application/json")
            .end(Json.encode(ResponseEntity.ok().body(tradeHistory).body))
    }
}