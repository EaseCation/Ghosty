package net.easecation.ghosty.recording.player.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedRotation(
    val yaw: Double,
    val pitch: Double
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ROTATION

    override fun processTo(ghost: PlaybackNPC) {
        ghost.teleport(ghost.location.also {
            it.yaw = this.yaw
            it.pitch = this.pitch
        })
    }

    override fun applyTo(node: PlayerRecordNode) = node.also {
        it.yaw = this.yaw
        it.pitch = this.pitch
    }

    companion object {
        @JvmStatic
        fun of(yaw: Double, pitch: Double) = PlayerUpdatedRotation(yaw, pitch)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedRotation> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedRotation> {
        override fun write(updated: PlayerUpdatedRotation, stream: BinaryStream) {
            stream.putFloat(updated.yaw.toFloat())
            stream.putFloat(updated.pitch.toFloat())
        }

        override fun read(stream: BinaryStream): PlayerUpdatedRotation {
            return PlayerUpdatedRotation(
                yaw = stream.float.toDouble(),
                pitch = stream.float.toDouble()
            )
        }
    }
}
