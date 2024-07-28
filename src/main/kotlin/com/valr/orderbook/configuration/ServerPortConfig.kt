package com.valr.orderbook.configuration

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.server.ConfigurableWebServerFactory
import org.springframework.boot.web.server.WebServerFactoryCustomizer
import org.springframework.context.annotation.Configuration

@Configuration
class ServerPortConfig : WebServerFactoryCustomizer<ConfigurableWebServerFactory> {

    @Value("\${server.port}")
    private var serverPort: Int = 0

    override fun customize(factory: ConfigurableWebServerFactory) {
        factory.setPort(serverPort)
    }
}