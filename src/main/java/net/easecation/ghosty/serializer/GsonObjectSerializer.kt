package net.easecation.ghosty.serializer

import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

private val GSON = Gson()

object GsonObjectSerializer : KSerializer<JsonObject> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("JsonObject", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): JsonObject {
        return GSON.fromJson(decoder.decodeString(), JsonObject::class.java)
    }

    override fun serialize(encoder: Encoder, value: JsonObject) {
        encoder.encodeString(GSON.toJson(value))
    }
}
