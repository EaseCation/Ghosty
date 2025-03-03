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
data class PlayerUpdatedArmor1(
    @Serializable(with = NukkitItemSerializer::class)
    val item: Item,
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ARMOR_1

    override fun processTo(ghost: PlaybackNPC) {
        val inv = ghost.armorInventory ?: return
        inv.setItem(ArmorInventory.SLOT_TORSO, item)
        inv.sendContents(ghost.viewers.values)
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { armor1 = item }

    companion object {
        @JvmStatic
        fun of(item: Item) = PlayerUpdatedArmor1(item)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedArmor1> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedArmor1> {
        override fun write(updated: PlayerUpdatedArmor1, stream: BinaryStream) {
            PersistenceBinaryStreamHelper.putItem(stream, updated.item)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedArmor1 = read(stream, 3)

        override fun read(stream: BinaryStream, version: Int): PlayerUpdatedArmor1 {
            return when (version) {
                0 -> PlayerUpdatedArmor1(stream.slot)
                1, 2, 3 -> PlayerUpdatedArmor1(PersistenceBinaryStreamHelper.getItem(stream))
                else -> throw IllegalArgumentException("unknown format version: $version")
            }
        }
    }
}
