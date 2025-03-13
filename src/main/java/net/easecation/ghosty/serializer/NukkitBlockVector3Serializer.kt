package net.easecation.ghosty.serializer

import cn.nukkit.math.BlockVector3
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


private val blockVector3Descriptor = buildClassSerialDescriptor(
    serialName = NukkitBlockVector3Serializer::class.java.packageName + "." + BlockVector3::class.java.simpleName
) {
    element<Int>("x")
    element<Int>("y")
    element<Int>("z")
}

object NukkitBlockVector3Serializer : KSerializer<BlockVector3> {
    override val descriptor: SerialDescriptor
        get() = blockVector3Descriptor

    override fun deserialize(decoder: Decoder): BlockVector3 {
        return decoder.decodeStructure(descriptor) {
            decodeElementIndex(descriptor)
            val x = this.decodeIntElement(descriptor, 0)
            decodeElementIndex(descriptor)
            val y = this.decodeIntElement(descriptor, 1)
            decodeElementIndex(descriptor)
            val z = this.decodeIntElement(descriptor, 2)
            BlockVector3(x, y, z)
        }
    }

    override fun serialize(encoder: Encoder, value: BlockVector3) {
        encoder.encodeStructure(descriptor) {
            encodeIntElement(descriptor, 0, value.x)
            encodeIntElement(descriptor, 1, value.y)
            encodeIntElement(descriptor, 2, value.z)
        }
    }
}