package net.easecation.ghosty.recording.player.updated

import cn.nukkit.entity.Entity
import cn.nukkit.entity.data.LongEntityData
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedDataFlagsExtended(
    val flags: Long
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_DATA_FLAGS_EXTENDED

    override fun processTo(ghost: PlaybackNPC) {
        ghost.setDataProperty(LongEntityData(Entity.DATA_FLAGS_EXTENDED, flags))
        ghost.recalculateBoundingBox()
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { dataFlagsExtended = flags }

    companion object {
        @JvmStatic
        fun of(flags: Long) = PlayerUpdatedDataFlagsExtended(flags)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedDataFlagsExtended> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedDataFlagsExtended> {
        override fun write(updated: PlayerUpdatedDataFlagsExtended, stream: BinaryStream) {
            stream.putVarLong(updated.flags)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedDataFlagsExtended =
            PlayerUpdatedDataFlagsExtended(stream.varLong)
    }
}
