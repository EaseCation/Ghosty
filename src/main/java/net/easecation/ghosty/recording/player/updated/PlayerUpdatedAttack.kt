package net.easecation.ghosty.recording.player.updated

import cn.nukkit.math.Mth
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.TextFormat
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.serializer.NukkitItemSerializer

internal fun Double.getDistanceString(): String {
    val color = when {
        this >= 3.6 -> TextFormat.RED
        this >= 3.0 -> TextFormat.YELLOW
        else -> TextFormat.GREEN
    }
    return color.toString() + Mth.round(this, 4)
}

@Serializable
data class PlayerUpdatedAttack(
    val attackTarget: Long,
) : PlayerUpdated {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ATTACK

    override fun processTo(ghost: PlaybackNPC) {
        val engine = ghost.engine ?: return
        if (!engine.displayAttackDistance) return
        val target = ghost.level?.actors?.values
            ?.filterIsInstance<PlaybackNPC>()
            ?.firstOrNull { it.originEntityId == attackTarget } ?: return
        val message = buildString {
            val distance = target.distance(ghost).getDistanceString()
            append("[Attack] ").append(distance).append(TextFormat.WHITE).append(" ")
            append("[${PlayerUpdatedPing.getDisplayPing(ghost.lastPing)}${TextFormat.WHITE}]${ghost.aliasName}")
            append("${TextFormat.RESET}${TextFormat.WHITE} -> ")
            append("[${PlayerUpdatedPing.getDisplayPing(target.lastPing)}${TextFormat.WHITE}]${target.aliasName}")
        }
        ghost.viewers.values.forEach { it.sendMessage(message) }
    }

    companion object {
        @JvmStatic
        fun of(attackTarget: Long) = PlayerUpdatedAttack(attackTarget)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedAttack> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedAttack> {
        override fun write(updated: PlayerUpdatedAttack, stream: BinaryStream) {
            stream.putEntityRuntimeId(updated.attackTarget)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedAttack =
            PlayerUpdatedAttack(stream.entityRuntimeId)
    }
}
