package net.easecation.ghosty.serializer

import cn.nukkit.math.Vector3f
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.*
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.encoding.decodeStructure
import kotlinx.serialization.encoding.encodeStructure


private val Vector3fDescriptor = buildClassSerialDescriptor(
    serialName = NukkitVector3fSerializer::class.java.packageName + "." + Vector3f::class.java.simpleName
) {
    element<Float>("x")
    element<Float>("y")
    element<Float>("z")
}

object NukkitVector3fSerializer : KSerializer<Vector3f> {
    override val descriptor: SerialDescriptor
        get() = Vector3fDescriptor

    override fun deserialize(decoder: Decoder): Vector3f {
        return decoder.decodeStructure(descriptor) {
            decodeElementIndex(descriptor)
            val x = this.decodeFloatElement(descriptor, 0)
            decodeElementIndex(descriptor)
            val y = this.decodeFloatElement(descriptor, 1)
            decodeElementIndex(descriptor)
            val z = this.decodeFloatElement(descriptor, 2)
            Vector3f(x, y, z)
        }
    }

    override fun serialize(encoder: Encoder, value: Vector3f) {
        encoder.encodeStructure(descriptor) {
            encodeFloatElement(descriptor, 0, value.x)
            encodeFloatElement(descriptor, 1, value.y)
            encodeFloatElement(descriptor, 2, value.z)
        }
    }
}