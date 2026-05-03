package net.easecation.ghosty.recording.player.updated

import cn.nukkit.Server
import cn.nukkit.math.Vector3f
import cn.nukkit.network.protocol.AnimatePacket
import cn.nukkit.network.protocol.EntityEventPacket
import cn.nukkit.utils.BinaryStream
import cn.nukkit.utils.TextFormat
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.playback.AttackDistanceCalculator
import net.easecation.ghosty.playback.PlayerPlaybackEngine
import net.easecation.ghosty.recording.player.PlayerRecordNode
import net.easecation.ghosty.recording.player.SkinlessPlayerRecord
import net.easecation.ghosty.serializer.NukkitVector3fSerializer
import kotlin.jvm.optionals.getOrNull

@Serializable
data class PlayerUpdatedEntityEvent(
    val event: Int,
    val data: Int,
    @Serializable(with = NukkitVector3fSerializer::class)
    val fireAtPosition: Vector3f?,
) : PlayerUpdated {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_ENTITY_EVENT

    override fun processTo(ghost: PlaybackNPC) {
        ghost.inventory ?: return

        Server.broadcastPacket(ghost.viewers.values, EntityEventPacket().apply {
            eid = ghost.id
            event = this@PlayerUpdatedEntityEvent.event
            data = this@PlayerUpdatedEntityEvent.data
        })

        // Legacy attack display logic
        val engine = ghost.engine as? PlayerPlaybackEngine ?: return
        val levelEngine = engine.levelPlaybackEngine ?: return
        if (
            !engine.displayAttackDistance
            || engine.record !is SkinlessPlayerRecord
            || (engine.record as SkinlessPlayerRecord).formatVersion >= 3
        ) return

        if (event == EntityEventPacket.HURT_ANIMATION) {
            levelEngine.playerPlaybackEngines
                .filter { it != engine }
                .forEach { otherEngine ->
                    val first = otherEngine.iteratorUnsafe
                        .peekBackwardFirstMatch { it is PlayerUpdatedAnimate }
                        .getOrNull() ?: return@forEach
                    if (first.entry !is PlayerUpdatedAnimate) return@forEach
                    if ((first.entry as PlayerUpdatedAnimate).action != AnimatePacket.Action.SWING_ARM.id) return@forEach
                    val attacker = otherEngine.npc
                        ?.takeIf { it.inventory?.itemInHand?.isSword == true } ?: return@forEach
                    val distance = AttackDistanceCalculator.calculate(attacker, ghost)
                    val distanceStr = distance.getDistanceString()
                    // 名字可能包含换行，显示时需要替换为单行
                    val attackerName = attacker.nameTag.replace('\n', ' ')
                    val victimName = ghost.nameTag.replace('\n', ' ')
                    val message =
                        "[Attack] $distanceStr${TextFormat.WHITE} $attackerName${TextFormat.RESET}${TextFormat.WHITE} -> $victimName"
                    ghost.viewers.values.forEach { it.sendMessage(message) }

                }
        }
    }

    override fun applyTo(node: PlayerRecordNode) = node

    companion object {
        @JvmStatic
        fun of(event: Int, data: Int) = of(event, data, null)

        @JvmStatic
        fun of(event: Int, data: Int, fireAtPosition: Vector3f? = null) = PlayerUpdatedEntityEvent(event, data, fireAtPosition)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedEntityEvent> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedEntityEvent> {
        override fun write(updated: PlayerUpdatedEntityEvent, stream: BinaryStream) {
            stream.putByte(updated.event.toByte())
            stream.putVarInt(updated.data)
            stream.putOptional(updated.fireAtPosition, BinaryStream::putVector3f)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedEntityEvent = read(stream, 4)

        override fun read(stream: BinaryStream, version: Int): PlayerUpdatedEntityEvent {
            val event = stream.byte
            val data = stream.varInt
            val fireAtPosition: Vector3f? = if (version >= 4) {
                stream.getOptional(BinaryStream::getVector3f)
            } else {
                null
            }
            return PlayerUpdatedEntityEvent(event, data, fireAtPosition)
        }
    }
}
