package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
data class EntityUpdatedSkinInfo(
    val geometryName: String,
    val skinDataHash: String
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_SKIN_INFO
    override fun processTo(entity: SimulatedEntity) {
        entity.setSkinInfo(SimulatedEntity.SkinInfo(geometryName, skinDataHash))
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        return node.also {
            it.skinInfo = SimulatedEntity.SkinInfo(geometryName, skinDataHash)
        }
    }

    companion object {
        @JvmStatic
        fun of(geometryName: String, skinDataHash: String) = EntityUpdatedSkinInfo(geometryName, skinDataHash)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedSkinInfo> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedSkinInfo> {
        override fun hasStates() = true
        override fun write(updated: EntityUpdatedSkinInfo, stream: BinaryStream) {
            stream.putString(updated.geometryName)
            stream.putString(updated.skinDataHash)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedSkinInfo(
            geometryName = stream.string,
            skinDataHash = stream.string
        )
    }
}
