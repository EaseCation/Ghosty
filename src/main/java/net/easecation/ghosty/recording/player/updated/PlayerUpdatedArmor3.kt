package net.easecation.ghosty.recording.player.updated

import cn.nukkit.inventory.ArmorInventory
import cn.nukkit.item.Item
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode
import net.easecation.ghosty.serializer.NukkitItemSerializer
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper

@Serializable
data class PlayerUpdatedArmor3(
    @Serializable(with = NukkitItemSerializer::class)
    val item: Item,
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ARMOR_3

    override fun processTo(ghost: PlaybackNPC) {
        val inv = ghost.armorInventory ?: return
        inv.setItem(ArmorInventory.SLOT_FEET, item)
        inv.sendContents(ghost.viewers.values)
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { armor3 = item }

    companion object {
        @JvmStatic
        fun of(item: Item) = PlayerUpdatedArmor3(item)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedArmor3> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedArmor3> {
        override fun write(updated: PlayerUpdatedArmor3, stream: BinaryStream) {
            PersistenceBinaryStreamHelper.putItem(stream, updated.item)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedArmor3 = read(stream, 3)

        override fun read(stream: BinaryStream, version: Int): PlayerUpdatedArmor3 {
            return when (version) {
                0 -> PlayerUpdatedArmor3(stream.slot)
                1, 2, 3 -> PlayerUpdatedArmor3(PersistenceBinaryStreamHelper.getItem(stream))
                else -> throw IllegalArgumentException("unknown format version: $version")
            }
        }
    }
}