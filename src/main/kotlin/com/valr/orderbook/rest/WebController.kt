package com.valr.orderbook.rest

import com.valr.orderbook.data.LimitOrderDTO
import com.valr.orderbook.data.MyError
import com.valr.orderbook.data.UserDTO
import com.valr.orderbook.security.JwtUtil
import com.valr.orderbook.service.OrderBookService
import com.valr.orderbook.service.TradeHistoryService
import com.valr.orderbook.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import javax.validation.Valid
import javax.validation.constraints.Min

@RestController
@RequestMapping("/api/")
class WebController @Autowired constructor(
        private val orderBookService: OrderBookService,
        private val tradeHistoryService: TradeHistoryService,
        private val userService: UserService,
        private val jwtUtil: JwtUtil
) {
    companion object {
        const val CURRENCY_PAIR_PATTERN = "[A-Za-z]{6}"
        const val CURRENCY_PAIR_VALIDATION_ERROR = "Invalid currency pair. Please provide a 6 character currency pair - valid example: BTCZAR | btczar."
    }

    @PostMapping("/user/login")
    fun loginUser(@Valid @RequestBody userDto: UserDTO): ResponseEntity<Any> {
        val optUser = userService.login(userDto.username, userDto.password)
        return if (optUser.isEmpty) {
            ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(MyError(-24, "Invalid login request. Invalid username or password."))
        } else {
            val existingUser = optUser.get()
            val response = mapOf("Bearer" to jwtUtil.generateToken(existingUser.username))
            ResponseEntity.ok(response)
        }
    }

    @GetMapping("{currencyPair}/orderbook")
    fun getOrderBook(@PathVariable currencyPair: String): ResponseEntity<Any> {
        val pattern = Regex(CURRENCY_PAIR_PATTERN)
        return if (!pattern.matches(currencyPair)) {
            ResponseEntity.badRequest().body(MyError(-21, CURRENCY_PAIR_VALIDATION_ERROR))
        } else {
            ResponseEntity.ok(orderBookService.getOrderBookBy(currencyPair))
        }
    }

    @PostMapping("/order/limit")
    fun createLimitOrder(@Valid @RequestBody limitOrder: LimitOrderDTO): ResponseEntity<Any> {
        return if (!limitOrder.currencyPair.matches(Regex(CURRENCY_PAIR_PATTERN)) || limitOrder.quantity <= 0 || limitOrder.price <= 0) {
            ResponseEntity.badRequest().body(MyError(-23, """
                Invalid limitOrder. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.
                Quantity and price must be greater than 0.
                Side must be either 'BUY' or 'SELL'.""".trimIndent()))
        } else {
            val executedOrder = orderBookService.createLimitOrder(limitOrder)
            executedOrder?.let { tradeHistoryService.addTradeOrder(it) }
            ResponseEntity.ok("Limit order created successfully.")
        }
    }

    @GetMapping("{currencyPair}/trades")
    fun getTradeHistory(
            @PathVariable currencyPair: String,
            @RequestParam(defaultValue = "0") @Min(0) skip: Int,
            @RequestParam(defaultValue = "10") @Min(0) limit: Int
    ): ResponseEntity<Any> {
        val pattern = Regex(CURRENCY_PAIR_PATTERN)
        return if (!pattern.matches(currencyPair)) {
            ResponseEntity.badRequest().body(MyError(-21, CURRENCY_PAIR_VALIDATION_ERROR))
        } else if (skip < 0 || limit < 0 || limit > 100) {
            ResponseEntity.badRequest().body(MyError(-22, "Invalid skip or limit value. Please provide a positive integer value for skip and limit (max limit is 100)."))
        } else {
            ResponseEntity.ok(tradeHistoryService.getTradeHistoryBy(currencyPair, skip, limit))
        }
    }
}