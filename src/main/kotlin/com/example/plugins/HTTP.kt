package com.example.plugins

import io.ktor.server.application.*
import io.ktor.server.plugins.swagger.*
import io.ktor.server.routing.*

fun Application.configureHTTP() {
    routing {
        swaggerUI(path = "swagger-ui", swaggerFile = "openapi/documentation.yaml") {
            version = "4.15.5"
        }
    }
}
