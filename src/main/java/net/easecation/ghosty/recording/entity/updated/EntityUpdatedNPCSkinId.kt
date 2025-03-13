package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedNPCSkinId(
    val data: Int
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_NPC_SKIN_ID
    override fun processTo(entity: SimulatedEntity) {
        entity.setDataProperty(IntEntityData(Entity.DATA_NPC_SKIN_ID, data))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.skinId = data
        return node
    }

    companion object {
        @JvmStatic
        fun of(data: Int) = EntityUpdatedNPCSkinId(data)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedNPCSkinId> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedNPCSkinId> {
        override fun hasStates(): Boolean = true
        override fun write(updated: EntityUpdatedNPCSkinId, stream: BinaryStream) {
            stream.putVarInt(updated.data)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedNPCSkinId(
            data = stream.varInt
        )
    }
}
