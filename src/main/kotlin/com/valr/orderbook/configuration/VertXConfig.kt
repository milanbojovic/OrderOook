package com.valr.orderbook.configuration

import com.valr.orderbook.rest.WebVerticle
import io.vertx.core.Vertx
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration


@Configuration
class VertXConfig(
    @Autowired
    var webVerticle: WebVerticle
) {

    @PostConstruct
    fun deployVerticle() {
        val vertx = Vertx.vertx()
        vertx.deployVerticle(webVerticle)
    }
}