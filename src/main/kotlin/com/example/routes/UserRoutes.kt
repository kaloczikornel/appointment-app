package com.example.routes

import com.example.controllers.UsersService
import com.example.models.Role
import com.example.models.UserCreateSchema
import com.example.models.UserLogin
import com.example.models.UserUpdateSchema
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.userRoutes(usersService: UsersService) {
    route("/user") {
        get("/profile") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val id = principal.payload.getClaim("id").asString()

            usersService.read(id)?.let { user ->
                call.respond(user)
            } ?: call.respond(HttpStatusCode.NotFound, "User not found with id $id")
        }
        put("/profile") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val id = principal.payload.getClaim("id").asString()
            val newUserData = call.receive<UserUpdateSchema>()

            val oldUserData = usersService.read(id) ?: let {
                call.respond(HttpStatusCode.NotFound, "User not found with id $id")
                return@put
            }

            val oldUserLoginData = UserLogin(
                email = oldUserData.email,
                password = newUserData.oldPassword
            )

            usersService.login(oldUserLoginData) ?: let {
                call.respond(HttpStatusCode.Unauthorized, "Old password is incorrect")
                return@put
            }

            val user = UserCreateSchema(
                email = newUserData.email ?: oldUserData.email,
                password = newUserData.password ?: newUserData.oldPassword,
                name = newUserData.name ?: oldUserData.name,
                role = newUserData.role ?: oldUserData.role
            )

            usersService.update(id, user)?.let {
                call.respond(it)
            } ?: call.respond(HttpStatusCode.NotFound, "User not found with id $id")
        }
        delete {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val id = principal.payload.getClaim("id").asString()

            usersService.delete(id)?.also {
                call.respond(HttpStatusCode.OK, "User deleted")
            } ?: call.respond(HttpStatusCode.NotFound, "User not found with id $id")
        }
    }
    get("/providers") {
        val principal = call.principal<JWTPrincipal>() ?: error("No principal")
        val id = principal.payload.getClaim("id").asString()
        val user = usersService.read(id) ?: let {
            call.respond(HttpStatusCode.NotFound, "User not found with id $id")
            return@get
        }
        if (user.role != Role.CLIENT) {
            call.respond(HttpStatusCode.Forbidden, "User is not a client, only clients can view providers")
            return@get
        }
        val providers = usersService.readProviders()
        call.respond(HttpStatusCode.OK, providers)
    }
}