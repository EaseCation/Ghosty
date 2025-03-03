package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedScale(
    val scale: Float
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_SCALE
    override fun processTo(entity: SimulatedEntity) {
        entity.setScale(scale)
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.scale = scale
        return node
    }

    companion object {
        @JvmStatic
        fun of(scale: Float) = EntityUpdatedScale(scale)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedScale> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedScale> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedScale, stream: BinaryStream) {
            stream.putFloat(updated.scale)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedScale(
            scale = stream.float
        )
    }
}
