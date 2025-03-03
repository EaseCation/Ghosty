package net.easecation.ghosty.serializer

import cn.nukkit.Server
import cn.nukkit.block.Block
import cn.nukkit.level.Position
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure
import kotlinx.serialization.serializer

private val idDescriptor = PrimitiveSerialDescriptor("id", PrimitiveKind.INT)
private val metaDescriptor = PrimitiveSerialDescriptor("meta", PrimitiveKind.INT)
private val levelDescriptor = PrimitiveSerialDescriptor("level", PrimitiveKind.STRING)
private val blockDescriptor = buildClassSerialDescriptor("Block") {
    element<Int>("id")
    element<Int>("meta")
    element("position", NukkitBlockVector3Serializer.descriptor)
    element<String?>("level", isOptional = true)
}

@OptIn(ExperimentalSerializationApi::class)
object NukkitBlockSerializer : KSerializer<Block> {
    override val descriptor: SerialDescriptor
        get() = blockDescriptor

    override fun deserialize(decoder: Decoder): Block {
        return decoder.decodeStructure(blockDescriptor) {
            decodeElementIndex(idDescriptor)
            val id = this.decodeIntElement(idDescriptor, 0)
            decodeElementIndex(metaDescriptor)
            val meta = this.decodeIntElement(metaDescriptor, 1)
            decodeElementIndex(blockVector3Descriptor)
            val vector3 = this.decodeSerializableElement(blockVector3Descriptor,2, NukkitBlockVector3Serializer)
            decodeElementIndex(levelDescriptor)
            val levelName = this.decodeNullableSerializableElement<String>(levelDescriptor, 3, serializer())
            val level = levelName?.let { Server.getInstance().getLevelByName(levelName) }
            Block.get(id, meta, Position(vector3.x.toDouble(), vector3.y.toDouble(), vector3.z.toDouble(), level))
        }
    }

    override fun serialize(encoder: Encoder, value: Block) {
        encoder.encodeStructure(blockDescriptor) {
            encodeIntElement(idDescriptor, 0, value.id)
            encodeIntElement(metaDescriptor, 1, value.damage)
            encodeSerializableElement(blockVector3Descriptor, 2, NukkitBlockVector3Serializer, value.asBlockVector3())
            val levelName = runCatching { value.level.name }.getOrNull()
            encodeNullableSerializableElement(levelDescriptor, 3, serializer(), levelName)
        }
    }
}