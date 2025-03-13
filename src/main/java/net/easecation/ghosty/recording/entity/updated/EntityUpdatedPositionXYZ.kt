package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.math.Vector3
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedPositionXYZ(
    val x: Double,
    val y: Double,
    val z: Double
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_POSITION_XYZ
    override fun processTo(entity: SimulatedEntity) {
        entity.teleport(entity.location.setComponents(x, y, z))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.x = x
        node.y = y
        node.z = z
        return node
    }

    fun asVector3(): Vector3 = Vector3(x, y, z)

    companion object {
        @JvmStatic
        fun of(x: Double, y: Double, z: Double) = EntityUpdatedPositionXYZ(x, y, z)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedPositionXYZ> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedPositionXYZ> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedPositionXYZ, stream: BinaryStream) {
            stream.putFloat(updated.x.toFloat())
            stream.putFloat(updated.y.toFloat())
            stream.putFloat(updated.z.toFloat())
        }
        override fun read(stream: BinaryStream) = EntityUpdatedPositionXYZ(
            stream.float.toDouble(),
            stream.float.toDouble(),
            stream.float.toDouble()
        )
    }
}