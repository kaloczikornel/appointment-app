package com.example.controllers

import com.example.models.AvailabilityCreateSchema
import com.example.models.AvailabilityFilter
import com.example.models.AvailabilitySchema
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.conversions.Bson
import org.bson.types.ObjectId
import java.text.SimpleDateFormat
import java.util.*

class AvailabilityService(private val database: MongoDatabase) {
    private var collection = database.getCollection("availabilities")
    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)


    // Create new availability
    suspend fun create(availability: AvailabilityCreateSchema): String = withContext(Dispatchers.IO) {
        val doc = availability.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Delete an availability by id
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }

    // Read an availability between a start and end time
    suspend fun readByTimeAndId(userId: String, startTime: Date, endTime: Date): List<AvailabilitySchema> =
        withContext(Dispatchers.IO) {
            collection.find(
                Filters.and(
                    Filters.eq("providerId", userId),
                    Filters.lte("startTime", df.format(startTime)),
                    Filters.gte("endTime", df.format(endTime))
                )
            )
                .map(AvailabilitySchema::fromDocument).toList()
        }

    // Read all availabilities
    suspend fun readAll(filters: AvailabilityFilter): List<AvailabilitySchema> = withContext(Dispatchers.IO) {
        val filterList: MutableList<Bson?> = mutableListOf()
        if (filters.providerId != null) {
            filterList.add(Filters.eq("providerId", filters.providerId))
        }
        if (filters.day != null) {
            filterList.add(Filters.eq("day", filters.day))
        }
        if (filters.afterTime != null) {
            filterList.add(Filters.gte("startTime", df.format(filters.afterTime)))
        }
        if (filters.beforeTime != null) {
            filterList.add(Filters.lte("endTime", df.format(filters.beforeTime)))
        }

        if (filterList.isNotEmpty()) {
            collection.find(Filters.and(filterList)).map(AvailabilitySchema::fromDocument)
                .toList()
        } else {
            collection.find().map(AvailabilitySchema::fromDocument).toList()
        }
    }

    // read all availabilities by provider id
    suspend fun readByProviderId(providerId: String): List<AvailabilitySchema> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("providerId", providerId)).map(AvailabilitySchema::fromDocument).toList()
    }


}