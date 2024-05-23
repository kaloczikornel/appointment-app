package com.example.routes

import com.example.controllers.AppointmentService
import com.example.controllers.AvailabilityService
import com.example.controllers.UsersService
import com.example.models.AppointmentRequest
import com.example.models.AppointmentSchema
import com.example.models.Role
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.appointmentRoutes(
    appointmentService: AppointmentService,
    availabilityService: AvailabilityService,
    usersService: UsersService
) {
    route("/appointment") {
        post {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val userId = principal.payload.getClaim("id").asString()
            val user = usersService.read(userId) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $userId")
                return@post
            }
            if (user.role != Role.CLIENT) {
                call.respond(HttpStatusCode.Forbidden, "User is not a client, only clients can create appointments")
                return@post
            }
            val appointmentRequest = call.receive<AppointmentRequest>()
            val availability = availabilityService.readByTimeAndId(
                appointmentRequest.providerId,
                appointmentRequest.startTime,
                appointmentRequest.endTime
            )
            if (availability.isEmpty()) {
                call.respond(HttpStatusCode.BadRequest, "No availability found")
                return@post
            }
            val isFree = appointmentService.readByProviderIdAndDate(
                appointmentRequest.providerId,
                appointmentRequest.startTime,
                appointmentRequest.endTime
            ).isEmpty()
            if (!isFree) {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Provider is not available between ${appointmentRequest.startTime} and ${appointmentRequest.endTime}"
                )
                return@post
            }
            val appointment = AppointmentSchema(
                userId,
                appointmentRequest.providerId,
                appointmentRequest.day,
                appointmentRequest.startTime,
                appointmentRequest.endTime
            )
            val appointmentId = appointmentService.create(appointment)
            call.respond(HttpStatusCode.Created, appointmentId)
        }
        get("/{id}") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val userId = principal.payload.getClaim("id").asString()
            usersService.read(userId) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $userId")
                return@get
            }
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Appointment id is required")
                return@get
            }
            val appointment = appointmentService.readById(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "Appointment not found with id $id")
                return@get
            }
            if (userId != appointment.clientId && userId != appointment.providerId) {
                call.respond(HttpStatusCode.Forbidden, "User is not authorized to view this appointment")
                return@get
            }

            call.respond(HttpStatusCode.OK, appointment)
        }
        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val userId = principal.payload.getClaim("id").asString()
            val user = usersService.read(userId) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $userId")
                return@delete
            }
            if (user.role != Role.CLIENT) {
                call.respond(HttpStatusCode.Forbidden, "User is not a client, only clients can delete appointments")
                return@delete
            }
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Appointment id is required")
                return@delete
            }
            if (appointmentService.readById(id)?.clientId != userId) {
                call.respond(HttpStatusCode.Forbidden, "User is not authorized to delete this appointment")
                return@delete
            }
            val deleted = appointmentService.delete(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "Appointment not found with id $id")
                return@delete
            }
            call.respond(HttpStatusCode.OK, deleted)
        }
    }
    get("/my-appointments") {
        val principal = call.principal<JWTPrincipal>() ?: error("No principal")
        val userId = principal.payload.getClaim("id").asString()
        val appointments = if (usersService.read(userId)?.role == Role.CLIENT) {
            appointmentService.readByClientId(userId)
        } else {
            appointmentService.readByProviderId(userId)
        }

        call.respond(HttpStatusCode.OK, appointments)
    }
}