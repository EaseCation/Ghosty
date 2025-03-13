package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.IntEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedMarkVariant(
    val data: Int
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_MARK_VARIANT
    override fun processTo(entity: SimulatedEntity) {
        entity.setDataProperty(IntEntityData(Entity.DATA_MARK_VARIANT, data))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.skinId = data
        return node
    }

    companion object {
        @JvmStatic
        fun of(data: Int) = EntityUpdatedMarkVariant(data)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedMarkVariant> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedMarkVariant> {
        override fun hasStates(): Boolean = true
        override fun write(updated: EntityUpdatedMarkVariant, stream: BinaryStream) {
            stream.putVarInt(updated.data)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedMarkVariant(
            data = stream.varInt
        )
    }
}
