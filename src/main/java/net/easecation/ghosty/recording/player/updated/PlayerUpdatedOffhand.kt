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
data class PlayerUpdatedOffhand(
    @Serializable(with = NukkitItemSerializer::class)
    val item: Item
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_OFFHAND

    override fun processTo(ghost: PlaybackNPC) {
        val inv = ghost.offhandInventory ?: return
        inv.item = item
        inv.sendContents(ghost.viewers.values)
    }

    override fun applyTo(node: PlayerRecordNode) = node.also {
        it.offhand = this.item
    }

    companion object {
        @JvmStatic
        fun of(item: Item) = PlayerUpdatedOffhand(item)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedOffhand> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedOffhand> {
        override fun write(updated: PlayerUpdatedOffhand, stream: BinaryStream) {
            PersistenceBinaryStreamHelper.putItem(stream, updated.item)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedOffhand = read(stream, 3)

        override fun read(stream: BinaryStream, version: Int): PlayerUpdatedOffhand {
            return when (version) {
                0 -> PlayerUpdatedOffhand(stream.slot)
                1, 2, 3 -> PlayerUpdatedOffhand(PersistenceBinaryStreamHelper.getItem(stream))
                else -> throw IllegalArgumentException("Unsupported format version: $version")
            }
        }
    }
}
