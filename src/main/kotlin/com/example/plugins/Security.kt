package com.example.plugins

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.*
import io.ktor.server.response.*

val config = ApplicationConfig("application.conf")


fun Application.configureSecurity() {
    // Please read the jwt property from the config file if you are using EngineMain
    val secret = config.property("jwt.secret").getString()
    authentication {
        jwt {
            verifier(
                JWT
                    .require(Algorithm.HMAC256(secret))
                    .build()
            )
            validate { credential ->
                if (credential.payload.getClaim("id").asString() != "") JWTPrincipal(credential.payload) else null
            }
            challenge { _, _ ->
                call.respond(HttpStatusCode.Unauthorized, "Token is not valid or has expired")
            }
        }
    }
}
