package net.easecation.ghosty.recording.player.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedTagName(
    var tn: String,
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_TAG_NAME

    override fun processTo(ghost: PlaybackNPC) {
        ghost.nameTag = tn
    }

    override fun applyTo(node: PlayerRecordNode) = node.apply { tagName = tn }

    companion object {
        @JvmStatic
        fun of(tn: String) = PlayerUpdatedTagName(tn)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedTagName> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedTagName> {
        override fun write(updated: PlayerUpdatedTagName, stream: BinaryStream) {
            stream.putString(updated.tn)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedTagName {
            return PlayerUpdatedTagName(stream.string)
        }
    }
}
