package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedRotation(
    val yaw: Double,
    val pitch: Double
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_ROTATION
    override fun processTo(entity: SimulatedEntity) {
        entity.setRotation(yaw, pitch)
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.yaw = yaw
        node.pitch = pitch
        return node
    }

    companion object {
        @JvmStatic
        fun of(yaw: Double, pitch: Double) = EntityUpdatedRotation(yaw, pitch)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedRotation> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedRotation> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedRotation, stream: BinaryStream) {
            stream.putFloat(updated.yaw.toFloat())
            stream.putFloat(updated.pitch.toFloat())
        }
        override fun read(stream: BinaryStream) = EntityUpdatedRotation(
            stream.float.toDouble(),
            stream.float.toDouble()
        )
    }
}