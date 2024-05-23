package com.example.routes

import com.example.controllers.AppointmentService
import com.example.controllers.AvailabilityService
import com.example.controllers.UsersService
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*


fun Application.configureRouting(services: Map<String, Any>) {
    val usersService = services["users"] as UsersService
    val availabilityService = services["availability"] as AvailabilityService
    val appointmentService = services["appointments"] as AppointmentService

    routing {
        get("/health") {
            call.respondText("OK")
        }
        authRoutes(usersService)
        authenticate {
            userRoutes(usersService)
            availabilityRoutes(availabilityService, usersService)
            appointmentRoutes(appointmentService, availabilityService, usersService)
        }
    }
}
