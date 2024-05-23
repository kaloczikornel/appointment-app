package com.example.serializers

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.descriptors.element
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.*
import org.bson.types.ObjectId

@Serializer(forClass = ObjectId::class)
object ObjectIdSerializer : KSerializer<ObjectId> {
    @OptIn(ExperimentalSerializationApi::class)
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("ObjectId") {
        element<String>("\$oid")
    }

    override fun serialize(encoder: Encoder, value: ObjectId) {
        encoder.encodeString(value.toHexString())

    }

    override fun deserialize(decoder: Decoder): ObjectId {
        val jsonDecoder = decoder as JsonDecoder
        val input = jsonDecoder.decodeJsonElement().jsonObject
        val oid = input["\$oid"]?.jsonPrimitive?.content
        return if (oid != null) ObjectId(oid) else ObjectId()
    }
}