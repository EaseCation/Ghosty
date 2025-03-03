package net.easecation.ghosty.recording.player.updated

import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.TextFormat.*
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedPing(
    val ping: Int
) : PlayerUpdated {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_PING

    override fun processTo(ghost: PlaybackNPC) {
        ghost.lastPing = ping
        if (ghost.engine?.displayPlayerPing == true) {
            val parts = ghost.nameTag.split("\n")
            ghost.nameTag = "${getDisplayPing(ping)}${WHITE}\n${parts.lastOrNull()}"
        }
    }

    override fun applyTo(node: PlayerRecordNode) = node.also {
        it.ping = this.ping
    }

    companion object {
        @JvmStatic
        fun of(ping: Int) = PlayerUpdatedPing(ping)

        @JvmStatic
        fun getDisplayPing(ping: Int): String = when {
            ping < 0 -> "N/A"
            ping < 200 -> "${GREEN}${ping}ms"
            ping < 500 -> "${YELLOW}${ping}ms"
            else -> "${RED}${ping}ms"
        }

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedPing> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedPing> {
        override fun write(updated: PlayerUpdatedPing, stream: BinaryStream) {
            stream.putUnsignedVarInt(updated.ping.toLong())
        }

        override fun read(stream: BinaryStream): PlayerUpdatedPing {
            return PlayerUpdatedPing(stream.unsignedVarInt.toInt())
        }
    }
}
