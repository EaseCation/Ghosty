package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedScoreTag(
    val tn: String
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_SCORE_TAG
    override fun processTo(entity: SimulatedEntity) {
        entity.setScoreTag(tn)
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.scoreTag = tn
        return node
    }

    companion object {
        @JvmStatic
        fun of(tn: String) = EntityUpdatedScoreTag(tn)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedScoreTag> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedScoreTag> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedScoreTag, stream: BinaryStream) {
            stream.putString(updated.tn)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedScoreTag(
            tn = stream.string
        )
    }
}
