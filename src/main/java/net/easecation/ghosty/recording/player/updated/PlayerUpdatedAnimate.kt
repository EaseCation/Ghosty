package net.easecation.ghosty.recording.player.updated

import cn.nukkit.Server
import cn.nukkit.network.protocol.AnimatePacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedAnimate(
    val action: Int = 0,
    val rowingTime: Float = 0f,
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId(): Int = PlayerUpdated.TYPE_ANIMATE

    override fun processTo(ghost: PlaybackNPC) {
        if (ghost.inventory != null) {
            val pk = AnimatePacket()
            pk.eid = ghost.id
            pk.action = AnimatePacket.Action.fromId(this.action)
            pk.rowingTime = this.rowingTime
            Server.broadcastPacket(ghost.viewers.values, pk)
        }
    }

    companion object {
        @JvmStatic
        fun of(action: Int, rowingTime: Float): PlayerUpdatedAnimate {
            return PlayerUpdatedAnimate(action, rowingTime)
        }

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedAnimate> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedAnimate> {
        override fun write(updated: PlayerUpdatedAnimate, stream: BinaryStream) {
            stream.putVarInt(updated.action)
            stream.putLFloat(updated.rowingTime)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedAnimate {
            return PlayerUpdatedAnimate(
                action = stream.varInt,
                rowingTime = stream.lFloat,
            )
        }
    }
}
