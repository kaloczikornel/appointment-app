package com.example.controllers

import com.example.models.Role
import com.example.models.UserCreateSchema
import com.example.models.UserLogin
import com.example.models.UsersResponse
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import com.mongodb.client.model.Filters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import org.bson.types.ObjectId

class UsersService(private val database: MongoDatabase) {
    private var collection: MongoCollection<Document> = database.getCollection("users")

    suspend fun login(user: UserLogin): String? = withContext(Dispatchers.IO) {
        collection.find(Filters.and(Filters.eq("email", user.email), Filters.eq("password", user.password)))
            .first()?.let { it["_id"].toString() }
    }

    // Create new user
    suspend fun create(user: UserCreateSchema): String = withContext(Dispatchers.IO) {
        val doc = user.toDocument()
        collection.insertOne(doc)
        doc["_id"].toString()
    }

    // Read a user
    suspend fun read(id: String): UsersResponse? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("_id", ObjectId(id))).first()?.let(UsersResponse::fromDocument)
    }

    // Read a user by email
    suspend fun findByEmail(email: String): UsersResponse? = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("email", email)).first()?.let(UsersResponse::fromDocument)
    }

    // Update a user
    suspend fun update(id: String, user: UserCreateSchema): UsersResponse? = withContext(Dispatchers.IO) {
        collection.findOneAndReplace(Filters.eq("_id", ObjectId(id)), user.toDocument())?.let(UsersResponse::fromDocument)
    }

    // Delete a user
    suspend fun delete(id: String): Document? = withContext(Dispatchers.IO) {
        collection.findOneAndDelete(Filters.eq("_id", ObjectId(id)))
    }

    // Read all users
    suspend fun readProviders(): List<UsersResponse> = withContext(Dispatchers.IO) {
        collection.find(Filters.eq("role", Role.PROVIDER )).mapNotNull { UsersResponse.fromDocument(it) }.toList()
    }
}