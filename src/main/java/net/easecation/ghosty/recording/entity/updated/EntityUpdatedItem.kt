package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.item.Item
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.entity.EntityRecordNode
import net.easecation.ghosty.serializer.NukkitItemSerializer
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper

@Serializable
data class EntityUpdatedItem(
    @Serializable(with = NukkitItemSerializer::class)
    val item: Item
) : EntityUpdated, UpdateWithState {
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_ITEM
    override fun processTo(entity: SimulatedEntity) {
        entity.item = item
    }

    override fun applyTo(node: EntityRecordNode): EntityRecordNode {
        node.item = item
        return node
    }

    companion object {
        @JvmStatic
        fun of(item: Item) = EntityUpdatedItem(item)

        @JvmStatic
        val ADAPTER: UpdateAdapter<EntityUpdatedItem> = Adapter
    }

    private object Adapter : UpdateAdapter<EntityUpdatedItem> {
        override fun hasStates(): Boolean = true
        override fun write(updated: EntityUpdatedItem, stream: BinaryStream) {
            PersistenceBinaryStreamHelper.putItem(stream, updated.item)
        }
        override fun read(stream: BinaryStream) = EntityUpdatedItem(
            item = PersistenceBinaryStreamHelper.getItem(stream)
        )
    }
}
