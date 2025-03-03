package net.easecation.ghosty.serializer

import cn.nukkit.math.BlockVector3
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


private val xDescriptor = PrimitiveSerialDescriptor("x", PrimitiveKind.INT)
private val yDescriptor = PrimitiveSerialDescriptor("y", PrimitiveKind.INT)
private val zDescriptor = PrimitiveSerialDescriptor("z", PrimitiveKind.INT)
internal val blockVector3Descriptor = buildClassSerialDescriptor("BlockVector3") {
    element<Int>("x")
    element<Int>("y")
    element<Int>("z")
}

object NukkitBlockVector3Serializer : KSerializer<BlockVector3> {
    override val descriptor: SerialDescriptor
        get() = blockVector3Descriptor

    override fun deserialize(decoder: Decoder): BlockVector3 {
        return decoder.decodeStructure(blockVector3Descriptor) {
            decodeElementIndex(xDescriptor)
            val x = this.decodeIntElement(xDescriptor, 0)
            decodeElementIndex(yDescriptor)
            val y = this.decodeIntElement(yDescriptor, 1)
            decodeElementIndex(zDescriptor)
            val z = this.decodeIntElement(zDescriptor, 2)
            BlockVector3(x, y, z)
        }
    }

    override fun serialize(encoder: Encoder, value: BlockVector3) {
        encoder.encodeStructure(blockVector3Descriptor) {
            encodeIntElement(xDescriptor, 0, value.x)
            encodeIntElement(yDescriptor, 1, value.y)
            encodeIntElement(zDescriptor, 2, value.z)
        }
    }
}