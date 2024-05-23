package com.example.routes

import com.example.controllers.AvailabilityService
import com.example.controllers.UsersService
import com.example.models.*
import com.example.utils.toDate
import com.example.utils.toDay
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.text.SimpleDateFormat
import java.util.*

fun Date.getDayOfWeek(): Int {
    val calendar = Calendar.getInstance()
    calendar.time = this
    return calendar.get(Calendar.DAY_OF_WEEK)
}

fun Route.availabilityRoutes(availabilityService: AvailabilityService, usersService: UsersService) {
    route("/availability") {
        post {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val id = principal.payload.getClaim("id").asString()

            val user = usersService.read(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $id")
                return@post
            }
            if (user.role != Role.PROVIDER) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "User is not a provider, only providers can create availability"
                )
                return@post
            }
            val availability = call.receive<AvailabilityCreateRequest>()

            val df = SimpleDateFormat("DD-MM-YYYY")
            if (df.format(availability.startTime) != df.format(availability.endTime)) {
                call.respond(HttpStatusCode.BadRequest, "Start and end time must be on the same day")
                return@post
            }

            if (availability.startTime.after(availability.endTime)) {
                call.respond(HttpStatusCode.BadRequest, "Start time must be before end time")
                return@post
            }
            when (availability.startTime.getDayOfWeek()) {
                1 -> Day.SUNDAY
                2 -> Day.MONDAY
                3 -> Day.TUESDAY
                4 -> Day.WEDNESDAY
                5 -> Day.THURSDAY
                6 -> Day.FRIDAY
                7 -> Day.SATURDAY
                else -> null
            }?. let {
                val availabilityObject =
                    AvailabilityCreateSchema(id, it, availability.startTime, availability.endTime)
                val availabilityId = availabilityService.create(availabilityObject)
                call.respond(HttpStatusCode.Created, availabilityId)
            }

        }
        delete("/{id}") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val userId = principal.payload.getClaim("id").asString()
            val user = usersService.read(userId) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $userId")
                return@delete
            }
            if (user.role != Role.PROVIDER) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "User is not a provider, only providers can delete availability"
                )
                return@delete
            }
            val id = call.parameters["id"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Availability id is required")
                return@delete
            }
            val deleted = availabilityService.delete(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "Availability not found with id $id")
                return@delete
            }
            call.respond(HttpStatusCode.OK, deleted)
        }
    }
    route("/availabilities") {
        get {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val id = principal.payload.getClaim("id").asString()
            usersService.read(id) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $id")
                return@get
            }

            try {
                val parameters = call.request.queryParameters
                val providerId = parameters["providerId"]
                val afterTime = parameters["afterTime"]?.toDate()
                val beforeTime = parameters["beforeTime"]?.toDate()
                val day = parameters["day"]?.toDay()

                val filters = AvailabilityFilter(
                    providerId = providerId,
                    afterTime = afterTime,
                    beforeTime = beforeTime,
                    day = day
                )


                availabilityService.readAll(filters).also {
                    call.respond(HttpStatusCode.OK, it)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.BadRequest, "Invalid query parameters")
            }

        }

        get("/{providerId}") {
            val principal = call.principal<JWTPrincipal>() ?: error("No principal")
            val userId = principal.payload.getClaim("id").asString()
            val user = usersService.read(userId) ?: run {
                call.respond(HttpStatusCode.NotFound, "User not found with id $userId")
                return@get
            }
            if (user.role != Role.CLIENT) {
                call.respond(
                    HttpStatusCode.Forbidden,
                    "User is not a client, only clients can view provider availabilities"
                )
                return@get
            }
            val providerId = call.parameters["providerId"] ?: run {
                call.respond(HttpStatusCode.BadRequest, "Provider id is required")
                return@get
            }
            availabilityService.readByProviderId(providerId).also {
                call.respond(HttpStatusCode.OK, it)
            }
        }
    }
}