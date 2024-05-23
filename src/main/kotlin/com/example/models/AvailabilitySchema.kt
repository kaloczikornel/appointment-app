package com.example.models

import com.example.serializers.DateSerializer
import com.example.serializers.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId
import java.util.*


enum class Day {
    MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY, SUNDAY
}

@Serializable
data class AvailabilitySchema(
    @SerialName("_id")
    @Serializable(with = ObjectIdSerializer::class) val id: ObjectId,
    val providerId: String,
    val day: Day,
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): AvailabilitySchema = json.decodeFromString(document.toJson())

    }
}

@Serializable
data class AvailabilityCreateSchema(
    @BsonId
    val providerId: String,
    val day: Day,
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))
}

@Serializable
data class AvailabilityCreateRequest(
    @Serializable(with = DateSerializer::class) val startTime: Date,
    @Serializable(with = DateSerializer::class) val endTime: Date,
)

data class AvailabilityFilter(
    val day: Day? = null,
    val providerId: String? = null,
    val afterTime: Date? = null,
    val beforeTime: Date? = null,
) {

}

