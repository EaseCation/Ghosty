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

private val itemDescriptor = buildClassSerialDescriptor(
    serialName = NukkitItemSerializer::class.java.packageName + "." + NkItem::class.java.simpleName
) {
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
            decodeElementIndex(itemDescriptor)
            val id = this.decodeIntElement(itemDescriptor, 0)
            decodeElementIndex(itemDescriptor)
            val count = this.decodeIntElement(itemDescriptor, 1)
            decodeElementIndex(itemDescriptor)
            val meta = this.decodeNullableSerializableElement(itemDescriptor, 2, serializer()) ?: 0
            decodeElementIndex(itemDescriptor)
            val name = this.decodeNullableSerializableElement(itemDescriptor, 3, serializer()) ?: UNKNOWN_STR
            NkItem(id, meta, count, name)
        }
    }

    override fun serialize(encoder: Encoder, value: NkItem) {
        encoder.encodeStructure(itemDescriptor) {
            encodeIntElement(itemDescriptor, 0, value.id)
            encodeIntElement(itemDescriptor, 1, value.count)
            encodeNullableSerializableElement(
                itemDescriptor,
                2,
                serializer(),
                if (value.hasMeta()) value.damage else null
            )
            encodeNullableSerializableElement(
                itemDescriptor,
                3,
                serializer(),
                if (value.name == UNKNOWN_STR) null else value.name
            )
        }
    }
}
