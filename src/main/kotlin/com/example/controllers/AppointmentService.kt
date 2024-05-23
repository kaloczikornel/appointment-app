package com.example.controllers

import com.example.models.AppointmentResponse
import com.example.models.AppointmentSchema
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

class AppointmentService(private val database: MongoDatabase) {
    private var collection = database.getCollection("appointments")
    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)


    // Create new appointment
    suspend fun create(appointment: AppointmentSchema): String = withContext(Dispatchers.IO) {
        val doc = appointment.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Delete an appointment by id
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }

    // Read an appointment by id
    suspend fun readById(id: String): AppointmentResponse? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).map(AppointmentResponse::fromDocument).firstOrNull()
    }

    suspend fun readByProviderId(providerId: String): List<AppointmentResponse> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("providerId", providerId)).map(AppointmentResponse::fromDocument).toList()
    }

    suspend fun readByClientId(clientId: String): List<AppointmentResponse> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("userId", clientId)).map(AppointmentResponse::fromDocument).toList()
    }

    // Update an appointment by id
    suspend fun update(id: String, appointment: AppointmentSchema): Boolean = withContext(Dispatchers.IO) {
        val doc = appointment.toDocument()
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), doc) != null
    }

    // Read all appointments
    suspend fun readAll(): List<AppointmentSchema> = withContext(Dispatchers.IO) {
        collection.find().map(AppointmentSchema::fromDocument).toList()
    }

    // Read all appointments by date
    suspend fun readByDate(date: String): List<AppointmentSchema> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("date", date)).map(AppointmentSchema::fromDocument).toList()
    }

    // Read all appointments by provider id and date
    suspend fun readByProviderIdAndDate(providerId: String, startTime: Date, endTime: Date): List<AppointmentSchema> =
        withContext(Dispatchers.IO) {
            collection.find(
                Filters.and(
                    Filters.eq("providerId", providerId),
                    Filters.or(
                        Filters.and(
                            Filters.lte("startTime", df.format(startTime)),
                            Filters.gte("endTime", df.format(startTime)),
                        ),
                        Filters.and(
                            Filters.lte("startTime", df.format(endTime)),
                            Filters.gte("endTime", df.format(endTime))
                        )
                    )

                )
            )
                .map(AppointmentSchema::fromDocument).toList()
        }
}