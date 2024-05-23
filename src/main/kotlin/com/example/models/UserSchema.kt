package com.example.models

import com.example.serializers.ObjectIdSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.bson.Document
import org.bson.types.ObjectId

enum class Role {
    CLIENT, PROVIDER
}

@Serializable
data class UserLogin(
    val email: String,
    val password: String,
)

@Serializable
data class UserCreateSchema(
    val name: String,
    val email: String,
    val password: String,
    val role: Role,
) {
    fun toDocument(): Document = Document.parse(Json.encodeToString(this))
}

@Serializable
data class UserUpdateSchema(
    val name: String? = null,
    val email: String? = null,
    val password: String? = null,
    val oldPassword: String,
    val role: Role? = null,
)

@Serializable
data class UsersResponse(
    @SerialName("_id")
    @Serializable(with = ObjectIdSerializer::class) val id: ObjectId,
    val name: String,
    val email: String,
    val role: Role,
) {
    companion object {
        private val json = Json { ignoreUnknownKeys = true }

        fun fromDocument(document: Document): UsersResponse = json.decodeFromString(document.toJson())
    }
}

@Serializable
data class UserToken(
    val token: String,
)