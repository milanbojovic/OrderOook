package com.valr.orderbook

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

/**
 * Main application class for the OrderBook application.
 * This class is annotated with @SpringBootApplication, which is a convenience annotation that adds:
 * - @Configuration: Tags the class as a source of bean definitions for the application context.
 * - @EnableAutoConfiguration: Tells Spring Boot to start adding beans based on classpath settings, other beans, and various property settings.
 * - @ComponentScan: Tells Spring to look for other components, configurations, and services in the com/valr/orderbook package, allowing it to find the controllers.
 */
@SpringBootApplication
class OrderBookApplication

/**
 * Main method to run the OrderBook application.
 * This method uses Spring Boot's runApplication function to launch the application.
 *
 * @param args Command line arguments passed to the application.
 */
fun main(args: Array<String>) {
    runApplication<OrderBookApplication>(*args)
}