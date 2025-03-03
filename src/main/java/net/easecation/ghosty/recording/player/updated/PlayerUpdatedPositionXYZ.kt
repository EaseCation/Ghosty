package net.easecation.ghosty.recording.player.updated

import cn.nukkit.math.Mth
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.TextFormat
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.UpdateWithState
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedPositionXYZ(
    val x: Double,
    val y: Double,
    val z: Double,
) : PlayerUpdated, UpdateWithState {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_POSITION_XYZ

    override fun processTo(ghost: PlaybackNPC) {
        ghost.teleport(ghost.location.setComponents(x, y, z))
        val engine = ghost.engine ?: return
        if (ghost.lastPosition != null) {
            if (engine.displayMovingSpeed && engine.tick % 10 == 0) {
                val delta = engine.tick - ghost.lastMoveTick
                val distance = ghost.location.distance(ghost.lastPosition) / delta * 20
                if (distance >= 0.01) {
                    val msg = buildString {
                        append("[Moving] ${Mth.round(distance, 3)}${TextFormat.WHITE} ")
                        append("[${PlayerUpdatedPing.getDisplayPing(ghost.lastPing)}${TextFormat.WHITE}]${ghost.aliasName}")
                    }
                    ghost.viewers.values.forEach { it.sendMessage(msg) }
                }
            }
            ghost.lastPosition = ghost.location
        } else {
            ghost.lastPosition = ghost.location
            ghost.lastMoveTick = engine.tick
        }
    }

    override fun applyTo(node: PlayerRecordNode) = node.also {
        it.x = this.x
        it.y = this.y
        it.z = this.z
    }

    fun asVector3() = Vector3(x, y, z)

    companion object {
        @JvmStatic
        fun of(x: Double, y: Double, z: Double) = PlayerUpdatedPositionXYZ(x, y, z)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedPositionXYZ> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedPositionXYZ> {
        override fun write(updated: PlayerUpdatedPositionXYZ, stream: BinaryStream) {
            stream.putFloat(updated.x.toFloat())
            stream.putFloat(updated.y.toFloat())
            stream.putFloat(updated.z.toFloat())
        }

        override fun read(stream: BinaryStream): PlayerUpdatedPositionXYZ {
            return PlayerUpdatedPositionXYZ(
                x = stream.float.toDouble(),
                y = stream.float.toDouble(),
                z = stream.float.toDouble(),
            )
        }
    }
}
