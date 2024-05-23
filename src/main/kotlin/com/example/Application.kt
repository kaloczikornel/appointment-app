package com.example

import com.example.controllers.configureServices
import com.example.database.configureDatabases
import com.example.plugins.configureHTTP
import com.example.plugins.configureSecurity
import com.example.plugins.configureSerialization
import com.example.routes.configureRouting
import io.ktor.server.application.*
import io.ktor.server.config.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*

val config = ApplicationConfig("application.conf")

fun main() {
    val isDev = config.property("ktor.development").getString() == "dev"
    if (isDev){
        System.setProperty("io.ktor.development", "true")
    }

    embeddedServer(
        Netty,
        port = 3000,
        host = "localhost",
        module = Application::module,
    )
        .start(wait = true)
}

fun Application.module() {
    configureSecurity()
    configureHTTP()
    configureSerialization()
    val db = configureDatabases()
    val services = configureServices(db)
    configureRouting(services)
}
