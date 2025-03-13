package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedVariant(
    val data: Int,
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_VARIANT
    override fun processTo(entity: SimulatedEntity) {
        entity.setDataProperty(IntEntityData(Entity.DATA_VARIANT, data))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.skinId = data
        return node
    }

    companion object {
        @JvmStatic
        fun of(data: Int) = EntityUpdatedVariant(data)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedVariant> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedVariant> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedVariant, stream: BinaryStream) {
            stream.putVarInt(updated.data)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedVariant(stream.varInt)
    }
}
