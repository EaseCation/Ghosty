package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedTagName(
    val tn: String,
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_TAG_NAME
    override fun processTo(entity: SimulatedEntity) {
        entity.setNameTag(tn)
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.tagName = tn
        return node
    }

    companion object {
        @JvmStatic
        fun of(tn: String) = EntityUpdatedTagName(tn)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedTagName> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedTagName> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedTagName, stream: BinaryStream) {
            stream.putString(updated.tn)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedTagName(stream.getString())
    }
}
