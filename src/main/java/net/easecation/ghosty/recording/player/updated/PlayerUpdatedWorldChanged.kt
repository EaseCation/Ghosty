package net.easecation.ghosty.recording.player.updated

import cn.nukkit.Server
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedWorldChanged(
    val wn: String
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_WORLD

    override fun processTo(ghost: PlaybackNPC) {
        ghost.location.level = Server.getInstance().getLevelByName(wn)
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { level = wn }

    companion object {
        @JvmStatic
        fun of(wn: String) = PlayerUpdatedWorldChanged(wn)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedWorldChanged> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedWorldChanged> {
        override fun write(updated: PlayerUpdatedWorldChanged, stream: BinaryStream) {
            stream.putString(updated.wn)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedWorldChanged {
            return PlayerUpdatedWorldChanged(stream.string)
        }
    }
}
