package com.example.models

import com.example.serializers.DateSerializer
import com.example.serializers.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId
import java.util.*

@Serializable
data class AppointmentSchema(
    val clientId: String,
    val providerId: String,
    val day: Day,
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))

    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): AppointmentSchema = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class AppointmentRequest(
    val providerId: String,
    val day: Day,
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
)

@Serializable
data class AppointmentResponse(
    @SerialName("_id")
    @Serializable(with = ObjectIdSerializer::class) val id: ObjectId,
    val clientId: String,
    val providerId: String,
    val day: Day,
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): AppointmentResponse = json.decodeFromString(document.toJson())
    }
}