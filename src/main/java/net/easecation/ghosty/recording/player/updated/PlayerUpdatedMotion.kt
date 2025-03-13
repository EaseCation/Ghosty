package net.easecation.ghosty.recording.player.updated

import cn.nukkit.math.Vector3
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedMotion(
    val x: Float,
    val y: Float,
    val z: Float
) : PlayerUpdated {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_MOTION

    override fun processTo(ghost: PlaybackNPC) = Unit

    override fun applyTo(node: PlayerRecordNode) = node

    fun asVector3() = Vector3(x.toDouble(), y.toDouble(), z.toDouble())

    companion object {
        @JvmStatic
        fun of(x: Float, y: Float, z: Float) = PlayerUpdatedMotion(x, y, z)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedMotion> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedMotion> {
        override fun write(updated: PlayerUpdatedMotion, stream: BinaryStream) {
            stream.putFloat(updated.x)
            stream.putFloat(updated.y)
            stream.putFloat(updated.z)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedMotion {
            val x = stream.float
            val y = stream.float
            val z = stream.float
            return PlayerUpdatedMotion(x, y, z)
        }
    }
}
