package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedSkinId(
    val data: Int
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_SKIN_ID
    override fun processTo(entity: SimulatedEntity) {
        entity.setDataProperty(IntEntityData(Entity.DATA_SKIN_ID, data))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.skinId = data
        return node
    }

    companion object {
        @JvmStatic
        fun of(data: Int) = EntityUpdatedSkinId(data)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedSkinId> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedSkinId> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedSkinId, stream: BinaryStream) {
            stream.putVarInt(updated.data)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedSkinId(
            data = stream.varInt
        )
    }
}
