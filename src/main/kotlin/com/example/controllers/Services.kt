package com.example.controllers

import com.mongodb.client.MongoDatabase
import io.ktor.server.application.*

fun Application.configureServices(mongoDatabase: MongoDatabase): Map<String, Any> {
    val usersService = UsersService(mongoDatabase)
    val availabilityService = AvailabilityService(mongoDatabase)
    val appointmentService = AppointmentService(mongoDatabase)

    val serviceMap = mapOf(
        "users" to usersService,
        "availability" to availabilityService,
        "appointments" to appointmentService
    )

    return serviceMap
}