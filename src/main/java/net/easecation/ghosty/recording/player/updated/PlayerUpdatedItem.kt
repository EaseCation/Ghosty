package net.easecation.ghosty.recording.player.updated

import cn.nukkit.item.Item
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode
import net.easecation.ghosty.serializer.NukkitItemSerializer
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper

@Serializable
data class PlayerUpdatedItem(
    @Serializable(with = NukkitItemSerializer::class)
    val item: Item
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ITEM

    override fun processTo(ghost: PlaybackNPC) {
        ghost.inventory?.let {
            it.itemInHand = item
            it.sendHeldItem(ghost.viewers.values)
        }
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { this.item = this@PlayerUpdatedItem.item }

    companion object {
        @JvmStatic
        fun of(item: Item) = PlayerUpdatedItem(item)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedItem> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedItem> {
        override fun write(updated: PlayerUpdatedItem, stream: BinaryStream) {
            PersistenceBinaryStreamHelper.putItem(stream, updated.item)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedItem = read(stream, 3)

        override fun read(stream: BinaryStream, version: Int): PlayerUpdatedItem {
            return when (version) {
                0 -> PlayerUpdatedItem(stream.slot)
                1, 2, 3 -> PlayerUpdatedItem(PersistenceBinaryStreamHelper.getItem(stream))
                else -> throw IllegalArgumentException("Unsupported format version: $version")
            }
        }
    }
}
