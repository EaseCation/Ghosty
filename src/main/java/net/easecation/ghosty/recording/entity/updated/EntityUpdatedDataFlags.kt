package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedDataFlags(
    val flags: Long = 0
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_DATA_FLAGS
    override fun processTo(entity: SimulatedEntity) {
        entity.setDataProperty(LongEntityData(Entity.DATA_FLAGS, this.flags))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.dataFlags = flags
        return node
    }

    companion object {
        @JvmStatic
        fun of(flags: Long): EntityUpdatedDataFlags {
            return EntityUpdatedDataFlags(flags)
        }

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedDataFlags> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedDataFlags> {
        override fun hasStates(): Boolean = true
        override fun write(updated: EntityUpdatedDataFlags, stream: BinaryStream) {
            stream.putVarLong(updated.flags)
        }
        override fun read(stream: BinaryStream): EntityUpdatedDataFlags = EntityUpdatedDataFlags(
            flags = stream.varLong
        )
    }
}
