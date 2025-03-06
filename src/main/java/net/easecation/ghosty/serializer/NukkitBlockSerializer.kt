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

private val blockDescriptor = buildClassSerialDescriptor(
    serialName = NukkitBlockSerializer::class.java.packageName + "." + Block::class.java.simpleName
) {
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
        return decoder.decodeStructure(descriptor) {
            decodeElementIndex(descriptor)
            val id = this.decodeIntElement(descriptor, 0)
            decodeElementIndex(descriptor)
            val meta = this.decodeIntElement(descriptor, 1)
            decodeElementIndex(descriptor)
            val vector3 = this.decodeSerializableElement(descriptor, 2, NukkitBlockVector3Serializer)
            decodeElementIndex(descriptor)
            val levelName = this.decodeNullableSerializableElement<String>(descriptor, 3, serializer())
            val level = levelName?.let { Server.getInstance().getLevelByName(levelName) }
            Block.get(id, meta, Position(vector3.x.toDouble(), vector3.y.toDouble(), vector3.z.toDouble(), level))
        }
    }

    override fun serialize(encoder: Encoder, value: Block) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.id)
            encodeIntElement(descriptor, 1, value.damage)
            encodeSerializableElement(descriptor, 2, NukkitBlockVector3Serializer, value.asBlockVector3())
            val levelName = runCatching { value.level.name }.getOrNull()
            encodeNullableSerializableElement(descriptor, 3, serializer(), levelName)
        }
    }
}