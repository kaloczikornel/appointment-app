package com.example.routes

import com.example.controllers.AppointmentService
import com.example.controllers.AvailabilityService
import com.example.controllers.UsersService
import com.example.models.AppointmentRequest
import com.example.models.AppointmentSchema
import com.example.models.AvailabilityCreateSchema
import com.example.models.Role
import com.example.utils.getDayOfWeek
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

            if (appointmentRequest.startTime.after(appointmentRequest.endTime)) {
                call.respond(HttpStatusCode.BadRequest, "Start time must be before end time")
                return@post
            }

            if (appointmentRequest.startTime == appointmentRequest.endTime) {
                call.respond(HttpStatusCode.BadRequest, "Start time and end time cannot be the same")
                return@post
            }

            val availability = availabilityService.readByTimeAndId(
                appointmentRequest.providerId,
                appointmentRequest.startTime,
                appointmentRequest.endTime
            ) ?: run {
                call.respond(
                    HttpStatusCode.BadRequest,
                    "Provider is not available between ${appointmentRequest.startTime} and ${appointmentRequest.endTime}"
                )
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
            val day = appointmentRequest.startTime.getDayOfWeek() ?: run {
                call.respond(HttpStatusCode.BadRequest, "Invalid date")
                return@post
            }
            val appointment = AppointmentSchema(
                userId,
                appointmentRequest.providerId,
                day,
                appointmentRequest.startTime,
                appointmentRequest.endTime
            )
            val appointmentId = appointmentService.create(appointment)

            // now lets remove the original availability and make new ones based on the appointment
            availabilityService.delete(availability.id.toString())

            //if the appointment starts exactly at the availability start time, we just need to update the end time
            if (availability.startTime == appointment.startTime) {
                val newAvailability = AvailabilityCreateSchema(
                    providerId = availability.providerId,
                    day = availability.day,
                    startTime = appointment.endTime,
                    endTime = availability.endTime
                )
                availabilityService.create(newAvailability)
            } else if (availability.endTime == appointment.endTime) {
                // if the appointment ends exactly at the availability end time, we just need to update the start time
                val newAvailability = AvailabilityCreateSchema(
                    providerId = availability.providerId,
                    day = availability.day,
                    startTime = availability.startTime,
                    endTime = appointment.startTime
                )
                availabilityService.create(newAvailability)
            } else {
                // otherwise we need to create two availabilities for the remaining time
                val firstAvailability = AvailabilityCreateSchema(
                    providerId = availability.providerId,
                    day = availability.day,
                    startTime = availability.startTime,
                    endTime = appointment.startTime
                )
                val secondAvailability = AvailabilityCreateSchema(
                    providerId = availability.providerId,
                    day = availability.day,
                    startTime = appointment.endTime,
                    endTime = availability.endTime
                )
                availabilityService.create(firstAvailability)
                availabilityService.create(secondAvailability)
            }

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
            appointmentService.delete(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "Appointment not found with id $id")
                return@delete
            }
            call.respond(HttpStatusCode.OK, "Appointment deleted")
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