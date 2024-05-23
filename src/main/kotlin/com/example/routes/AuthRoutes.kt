package com.example.routes

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.example.controllers.UsersService
import com.example.models.UserCreateSchema
import com.example.models.UserLogin
import com.example.models.UserToken
import com.example.plugins.config
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Route.authRoutes(usersService: UsersService) {
    val secret = config.property("jwt.secret").getString()

    post("/register") {
        val user = call.receive<UserCreateSchema>()

        val existingUser = usersService.findByEmail(user.email)
        if (existingUser != null) {
            call.respond(HttpStatusCode.BadRequest, "User already exists")
            return@post
        }

        val validatedUser = UserCreateSchema(
            email = user.email,
            password = user.password,
            name = user.name,
            role = user.role
        )

        usersService.create(validatedUser).also { userId ->
            JWT.create()
                .withClaim("id", userId)
                .sign(Algorithm.HMAC256(secret)).let { token ->
                    val response = UserToken(token)
                    call.respond(response)
                }
        }
    }
    post("/login") {
        val user = call.receive<UserLogin>()

        usersService.login(user)?.also {
            val token = JWT.create()
                .withClaim("id", it)
                .sign(Algorithm.HMAC256(secret))
            val response = UserToken(token)
            call.respond(response)
        } ?: call.respond(HttpStatusCode.Unauthorized, "Invalid credentials")
    }
}