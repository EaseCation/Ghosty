package net.easecation.ghosty.serializer

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer
import cn.nukkit.item.Item as NkItem

// from nukkit Item.java
private const val UNKNOWN_STR = "UNKNOWN_STR"

private val idDescriptor = PrimitiveSerialDescriptor("id", PrimitiveKind.INT)
private val countDescriptor = PrimitiveSerialDescriptor("count", PrimitiveKind.INT)
private val metaDescriptor = PrimitiveSerialDescriptor("meta", PrimitiveKind.INT)
private val nameDescriptor = PrimitiveSerialDescriptor("name", PrimitiveKind.STRING)
private val itemDescriptor = buildClassSerialDescriptor("Item") {
    element<Int>("id")
    element<Int>("count")
    element<Int?>("meta", isOptional = true)
    element<String?>("name", isOptional = true)
}

@OptIn(ExperimentalSerializationApi::class)
object NukkitItemSerializer : KSerializer<NkItem> {
    override val descriptor: SerialDescriptor
        get() = itemDescriptor

    override fun deserialize(decoder: Decoder): NkItem {
        return decoder.decodeStructure(itemDescriptor) {
            decodeElementIndex(idDescriptor)
            val id = this.decodeIntElement(idDescriptor, 0)
            decodeElementIndex(countDescriptor)
            val count = this.decodeIntElement(countDescriptor, 1)
            decodeElementIndex(metaDescriptor)
            val meta = this.decodeNullableSerializableElement(metaDescriptor, 2, serializer()) ?: 0
            decodeElementIndex(nameDescriptor)
            val name = this.decodeNullableSerializableElement(nameDescriptor, 3, serializer()) ?: UNKNOWN_STR
            NkItem(id, meta, count, name)
        }
    }

    override fun serialize(encoder: Encoder, value: NkItem) {
        encoder.encodeStructure(itemDescriptor) {
            encodeIntElement(idDescriptor, 0, value.id)
            encodeIntElement(countDescriptor, 1, value.count)
            encodeNullableSerializableElement(metaDescriptor, 2, serializer(), if (value.hasMeta()) value.damage else null)
            encodeNullableSerializableElement(nameDescriptor, 3, serializer(), if (value.name == UNKNOWN_STR) null else value.name)
        }
    }
}
