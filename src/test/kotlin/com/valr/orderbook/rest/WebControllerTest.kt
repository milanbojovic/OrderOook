package com.valr.orderbook.rest

import com.fasterxml.jackson.databind.ObjectMapper
import com.valr.orderbook.data.LimitOrderDTO
import com.valr.orderbook.data.TradeHistory
import com.valr.orderbook.data.User
import com.valr.orderbook.data.UserDTO
import com.valr.orderbook.data.enum.Side
import com.valr.orderbook.security.JwtUtil
import com.valr.orderbook.service.OrderBookService
import com.valr.orderbook.service.TradeHistoryService
import com.valr.orderbook.service.UserService
import com.valr.orderbook.util.TestHelper.BTC_ZAR
import com.valr.orderbook.util.TestHelper.createOrder
import com.valr.orderbook.util.TestHelper.createOrderBook
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import org.mockito.Mockito.`when` as whenever

@SpringBootTest
class WebControllerTest {

    val skip: String = "skip"
    val limit: String = "limit"

    @Mock
    private lateinit var jwtUtil: JwtUtil

    @Mock
    private lateinit var orderBookService: OrderBookService

    @Mock
    private lateinit var tradeHistoryService: TradeHistoryService

    @Mock
    private lateinit var userService: UserService

    @InjectMocks
    private lateinit var webController: WebController

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    private lateinit var mockMvc: MockMvc

    @BeforeEach
    fun setup() {
        MockitoAnnotations.openMocks(this)
        webController = WebController(orderBookService, tradeHistoryService, userService, jwtUtil)
        mockMvc = MockMvcBuilders.standaloneSetup(webController).build()
    }

    @Test
    fun login_with_valid_credentials_returns_token() {
        whenever(jwtUtil.generateToken(anyString())).thenReturn("mockToken")
        whenever(userService.login(anyString(), anyString())).thenReturn(Optional.of(createTestUser()))

        val mvcResult: MvcResult = mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(UserDTO("admin", "admin"))))
            .andExpect(status().isOk)
            .andReturn()

        val actualResponse = mvcResult.response.contentAsString
        assertTrue(actualResponse.contains("Bearer") && actualResponse.contains("mockToken"))
    }

    private fun createTestUser(): User {
        return User("John", "Doe", "john.doe@valr.com", "john.doe", "pass")
    }

    @Test
    fun login_with_invalid_credentials_returns_unauthorized() {
        whenever(userService.login(anyString(), anyString())).thenReturn(Optional.empty())
        val mvcResult: MvcResult = mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(UserDTO("admin1", "admin1"))))
            .andExpect(status().isUnauthorized)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertTrue(actualResponse.contains("Invalid username or password."))
    }

    @Test
    fun login_with_null_username_returns_bad_request() {
        val mvcResult: MvcResult = mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(UserDTO(null.toString(), "admin"))))
            .andExpect(status().isUnauthorized)
            .andReturn()

        val actualResponse = mvcResult.response.contentAsString
        assertTrue(actualResponse.contains("Invalid login request. Invalid username or password."))
    }

    @Test
    fun login_with_null_pass_returns_bad_request() {
        val mvcResult: MvcResult = mockMvc.perform(post("/api/user/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(UserDTO("admin", null.toString()))))
            .andExpect(status().isUnauthorized)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertTrue(actualResponse.contains("Invalid login request. Invalid username or password."))
    }

    @Test
    fun get_orderbook_with_valid_currency_pair_returns_orderbook() {
        val orderBook = createOrderBook()
        whenever(orderBookService.getOrderBookBy(anyString())).thenReturn(orderBook)
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/orderbook")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals(objectMapper.writeValueAsString(orderBook), actualResponse)
        verify(orderBookService).getOrderBookBy(BTC_ZAR)
    }

    @Test
    fun get_orderbook_with_invalid_currency_pair_returns_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR1/orderbook")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-21,\"message\":\"Invalid currency pair. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.\"}", actualResponse)
    }

    @Test
    fun get_orderbook_with_special_character_in_currency_pair_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTC@AR/orderbook")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-21,\"message\":\"Invalid currency pair. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.\"}", actualResponse)
    }

    @Test
    fun create_limit_order_with_valid_data_returns_success() {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.5, 100, BTC_ZAR)
        val executedOrder = createOrder(Side.SELL, 0.5, 100, BTC_ZAR)
        whenever(orderBookService.createLimitOrder(any())).thenReturn(executedOrder)

        val mvcResult: MvcResult = mockMvc.perform(post("/api/order/limit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(limitOrder)))
            .andExpect(status().isOk)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("Limit order created successfully.", actualResponse)
        verify(orderBookService).createLimitOrder(any())
        verify(tradeHistoryService).addTradeOrder(executedOrder)
    }

    @Test
    fun create_limit_order_with_valid_data_but_no_executed_order_returns_success() {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.5, 100, BTC_ZAR)
        whenever(orderBookService.createLimitOrder(any())).thenReturn(null)

        val mvcResult: MvcResult = mockMvc.perform(post("/api/order/limit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(limitOrder)))
            .andExpect(status().isOk)
            .andReturn()

        val actualResponse = mvcResult.response.contentAsString
        assertEquals("Limit order created successfully.", actualResponse)
        verify(orderBookService).createLimitOrder(any())
        verify(tradeHistoryService, never()).addTradeOrder(any())
    }

    @Test
    fun create_limit_order_with_invalid_data_returns_bad_request() {
        val limitOrder = LimitOrderDTO(Side.SELL, 0.0, 0, BTC_ZAR)

        val mvcResult: MvcResult = mockMvc.perform(post("/api/order/limit")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(limitOrder)))
            .andExpect(status().isBadRequest)
            .andReturn()

        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-23,\"message\":\"Invalid limitOrder. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.\\nQuantity and price must be greater than 0.\\nSide must be either 'BUY' or 'SELL'.\"}", actualResponse)
        verify(orderBookService, never()).createLimitOrder(any())
        verify(tradeHistoryService, never()).addTradeOrder(any())
    }

    @Test
    fun get_tradehistory_with_valid_parameters_returns_tradehistory() {
        val tradeHistory = TradeHistory()
        whenever(tradeHistoryService.getTradeHistoryBy(anyString(), anyInt(), anyInt())).thenReturn(tradeHistory)
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/trades")
            .param(skip, "5")
            .param(limit, "17")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals(objectMapper.writeValueAsString(tradeHistory), actualResponse)
        verify(tradeHistoryService).getTradeHistoryBy(eq(BTC_ZAR), eq(5), eq(17))
    }

    @Test
    fun get_tradehistory_withhout_limit_and_skip_parames_calls_with_defaults() {
        val tradeHistory = TradeHistory()
        whenever(tradeHistoryService.getTradeHistoryBy(anyString(), anyInt(), anyInt())).thenReturn(tradeHistory)
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/trades")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals(objectMapper.writeValueAsString(tradeHistory), actualResponse)
        verify(tradeHistoryService).getTradeHistoryBy(eq(BTC_ZAR), eq(0), eq(10))
    }

    @Test
    fun get_tradehistory_with_invalid_currency_pair_returns_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTC@AR/trades")
            .param(skip, "0")
            .param(limit, "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-21,\"message\":\"Invalid currency pair. Please provide a 6 character currency pair - valid example: BTCZAR | btczar.\"}", actualResponse)
    }

    @Test
    fun get_tradehistory_with_negative_skip_returns_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/trades")
            .param(skip, "-1")
            .param(limit, "10")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-22,\"message\":\"Invalid skip or limit value. Please provide a positive integer value for skip and limit (max limit is 100).\"}", actualResponse)
    }

    @Test
    fun get_tradehistory_with_negative_limit_returns_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/trades")
            .param(skip, "0")
            .param(limit, "-1")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-22,\"message\":\"Invalid skip or limit value. Please provide a positive integer value for skip and limit (max limit is 100).\"}", actualResponse)
    }

    @Test
    fun get_tradehistory_with_limit_exceeding_max_returns_error() {
        val mvcResult: MvcResult = mockMvc.perform(get("/api/BTCZAR/trades")
            .param(skip, "0")
            .param(limit, "101")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest)
            .andReturn()
        val actualResponse = mvcResult.response.contentAsString
        assertEquals("{\"code\":-22,\"message\":\"Invalid skip or limit value. Please provide a positive integer value for skip and limit (max limit is 100).\"}", actualResponse)
    }
}