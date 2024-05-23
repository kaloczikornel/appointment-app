package com.example.serializers

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.*

@Serializer(forClass = Date::class)
object DateSerializer : KSerializer<Date> {
    private val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US)

    override fun serialize(encoder: Encoder, value: Date) {
        encoder.encodeString(df.format(value))
    }

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Date {
        return df.parse(decoder.decodeString())!!
    }
}