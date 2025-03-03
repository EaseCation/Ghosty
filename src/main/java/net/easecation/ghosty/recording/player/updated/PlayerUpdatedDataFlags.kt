package net.easecation.ghosty.recording.player.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedDataFlags(
    val flags: Long
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_DATA_FLAGS

    override fun processTo(ghost: PlaybackNPC) {
        ghost.setDataProperty(LongEntityData(Entity.DATA_FLAGS, flags))
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { dataFlags = flags }

    companion object {
        @JvmStatic
        fun of(flags: Long) = PlayerUpdatedDataFlags(flags)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedDataFlags> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedDataFlags> {
        override fun write(updated: PlayerUpdatedDataFlags, stream: BinaryStream) {
            stream.putVarLong(updated.flags)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedDataFlags =
            PlayerUpdatedDataFlags(stream.varLong)
    }
}
