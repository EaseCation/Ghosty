package net.easecation.ghosty.recording.player.updated

import cn.nukkit.math.Mth
import cn.nukkit.math.Vector3
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.TextFormat
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.playback.AttackDistanceCompensationResult

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
        val victim = ghost.level?.entities
            ?.filterIsInstance<PlaybackNPC>()
            ?.firstOrNull { it.originEntityId == attackTarget } ?: return
        // 名字可能包含换行，显示时需要替换为单行
        // ghost是攻击者，victim是受击者
        val attackerName = ghost.nameTag.replace('\n', ' ')
        val victimName = victim.nameTag.replace('\n', ' ')
        val message = buildString {
            val distance = ghost.distance(victim).getDistanceString()
            val compensatedDistance = when (
                val compensation = engine.levelPlaybackEngine?.attackDistanceCompensator?.calculate(
                    engine,
                    attackTarget,
                    engine.tick,
                    Vector3(ghost.x, ghost.y, ghost.z),
                )
            ) {
                is AttackDistanceCompensationResult.Available -> compensation.distance.getDistanceString()
                is AttackDistanceCompensationResult.Unavailable,
                null -> TextFormat.GRAY.toString() + "N/A"
            }
            append("[Attack] ")
            append("标准:").append(distance).append(TextFormat.WHITE).append(" ")
            append("延迟补偿攻击距离:").append(compensatedDistance).append(TextFormat.WHITE).append(" ")
            append("[${PlayerUpdatedPing.getDisplayPing(ghost.lastPing)}${TextFormat.WHITE}]${attackerName}")
            append("${TextFormat.RESET}${TextFormat.WHITE} -> ")
            append("[${PlayerUpdatedPing.getDisplayPing(victim.lastPing)}${TextFormat.WHITE}]${victimName}")
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
