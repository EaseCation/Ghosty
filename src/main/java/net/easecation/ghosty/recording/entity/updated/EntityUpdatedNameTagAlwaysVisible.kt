package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedNameTagAlwaysVisible(
    val visible: Boolean
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_NAMETAG_ALWAYS_VISIBLE
    override fun processTo(entity: SimulatedEntity) {
        entity.isNameTagAlwaysVisible = visible
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.isNameTagAlwaysVisible = visible
        return node
    }

    companion object {
        @JvmStatic
        fun of(visible: Boolean) = EntityUpdatedNameTagAlwaysVisible(visible)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedNameTagAlwaysVisible> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedNameTagAlwaysVisible> {
        override fun hasStates(): Boolean = true
        override fun write(updated: EntityUpdatedNameTagAlwaysVisible, stream: BinaryStream) {
            stream.putByte(if (updated.visible) 1 else 0)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedNameTagAlwaysVisible(
            visible = stream.boolean
        )
    }
}
