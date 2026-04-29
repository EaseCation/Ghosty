package net.easecation.ghosty.playback

import cn.nukkit.math.Vector3
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.easecation.ghosty.recording.player.PlayerRecord
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPing
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPositionXYZ
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedWorldChanged
import java.util.IdentityHashMap

class AttackDistanceCompensator private constructor(playerRecords: Collection<PlayerRecord>) {

    private val historiesByRecord: Map<PlayerRecord, PlayerHistory> = IdentityHashMap<PlayerRecord, PlayerHistory>().apply {
        playerRecords.forEach { playerRecord ->
            put(playerRecord, PlayerHistory.from(playerRecord))
        }
    }

    private val historiesByEntityId: Long2ObjectMap<PlayerHistory> = Long2ObjectOpenHashMap<PlayerHistory>().apply {
        historiesByRecord.values.forEach { history ->
            if (history.originEntityId > 0) {
                put(history.originEntityId, history)
            }
        }
    }

    @JvmOverloads
    fun calculate(
        attackerEngine: PlayerPlaybackEngine,
        victimEntityId: Long,
        attackTick: Int,
        fallbackAttackerPosition: Vector3? = null,
    ): AttackDistanceCompensationResult {
        return calculate(attackerEngine.record, victimEntityId, attackTick, fallbackAttackerPosition)
    }

    @JvmOverloads
    fun calculate(
        attackerRecord: PlayerRecord,
        victimEntityId: Long,
        attackTick: Int,
        fallbackAttackerPosition: Vector3? = null,
    ): AttackDistanceCompensationResult {
        val attackerHistory = historiesByRecord[attackerRecord]
            ?: return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.ATTACKER_NOT_FOUND)
        val victimHistory = historiesByEntityId[victimEntityId]
            ?: return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.TARGET_NOT_FOUND)
        val attackerPingMs = attackerHistory.samplePing(attackTick)
            ?: return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.ATTACKER_PING_UNAVAILABLE)
        val visualTargetTick = attackTick - attackerPingMs / TICK_DURATION_MS
        val attackerPosition = attackerHistory.samplePosition(attackTick.toDouble())
            ?: fallbackAttackerPosition
            ?: return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.ATTACKER_POSITION_UNAVAILABLE)
        val victimPosition = victimHistory.samplePosition(visualTargetTick)
            ?: return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.TARGET_POSITION_UNAVAILABLE)
        val attackerWorld = attackerHistory.sampleWorld(attackTick.toDouble())
        val victimWorld = victimHistory.sampleWorld(visualTargetTick)
        if (attackerWorld != null && victimWorld != null && attackerWorld != victimWorld) {
            return AttackDistanceCompensationResult.Unavailable(AttackDistanceCompensationUnavailableReason.WORLD_MISMATCH)
        }
        return AttackDistanceCompensationResult.Available(
            distance = attackerPosition.distance(victimPosition),
            attackerPingMs = attackerPingMs,
            visualTargetTick = visualTargetTick,
        )
    }

    companion object {
        @JvmStatic
        fun fromPlaybackEngines(playerPlaybackEngines: Collection<PlayerPlaybackEngine>): AttackDistanceCompensator {
            return AttackDistanceCompensator(playerPlaybackEngines.map { it.record })
        }

        @JvmStatic
        fun fromPlayerRecords(playerRecords: Collection<PlayerRecord>): AttackDistanceCompensator {
            return AttackDistanceCompensator(playerRecords)
        }
    }
}

sealed class AttackDistanceCompensationResult {

    data class Available(
        val distance: Double,
        val attackerPingMs: Int,
        val visualTargetTick: Double,
    ) : AttackDistanceCompensationResult()

    data class Unavailable(
        val reason: AttackDistanceCompensationUnavailableReason,
    ) : AttackDistanceCompensationResult()
}

enum class AttackDistanceCompensationUnavailableReason {
    ATTACKER_NOT_FOUND,
    TARGET_NOT_FOUND,
    ATTACKER_PING_UNAVAILABLE,
    ATTACKER_POSITION_UNAVAILABLE,
    TARGET_POSITION_UNAVAILABLE,
    WORLD_MISMATCH,
}

private const val TICK_DURATION_MS = 50.0
private const val MAX_INTERPOLATION_TICKS = 20

private interface TickSample {
    val tick: Int
}

private data class PositionSample(
    override val tick: Int,
    val x: Double,
    val y: Double,
    val z: Double,
) : TickSample {
    fun toVector3(): Vector3 {
        return Vector3(x, y, z)
    }
}

private data class PingSample(
    override val tick: Int,
    val ping: Int,
) : TickSample

private data class WorldSample(
    override val tick: Int,
    val worldName: String,
) : TickSample

private data class PlayerHistory(
    val originEntityId: Long,
    val positions: List<PositionSample>,
    val pings: List<PingSample>,
    val worlds: List<WorldSample>,
) {

    fun samplePosition(targetTick: Double): Vector3? {
        val previousIndex = previousIndex(positions, targetTick)
        if (previousIndex < 0) {
            return null
        }
        val previous = positions[previousIndex]
        if (previous.tick.toDouble() == targetTick || previousIndex == positions.lastIndex) {
            return previous.toVector3()
        }
        val next = positions[previousIndex + 1]
        if (next.tick - previous.tick > MAX_INTERPOLATION_TICKS) {
            return previous.toVector3()
        }
        val percent = (targetTick - previous.tick) / (next.tick - previous.tick)
        return Vector3(
            previous.x + (next.x - previous.x) * percent,
            previous.y + (next.y - previous.y) * percent,
            previous.z + (next.z - previous.z) * percent,
        )
    }

    fun samplePing(targetTick: Int): Int? {
        val previousIndex = previousIndex(pings, targetTick.toDouble())
        if (previousIndex < 0) {
            return null
        }
        val ping = pings[previousIndex].ping
        return ping.takeIf { it >= 0 }
    }

    fun sampleWorld(targetTick: Double): String? {
        val previousIndex = previousIndex(worlds, targetTick)
        if (previousIndex < 0) {
            return null
        }
        return worlds[previousIndex].worldName
    }

    companion object {
        fun from(playerRecord: PlayerRecord): PlayerHistory {
            val positions = mutableListOf<PositionSample>()
            val pings = mutableListOf<PingSample>()
            val worlds = mutableListOf<WorldSample>()
            val iterator = playerRecord.iterator()
            generateSequence(iterator.peekTick().takeIf { it >= 0 }) {
                iterator.peekTick().takeIf { it >= 0 }
            }.forEach { tick ->
                iterator.pollToTick(tick).forEach { updated ->
                    when (updated) {
                        is PlayerUpdatedPositionXYZ -> positions.appendReplacingSameTick(
                            PositionSample(tick, updated.x, updated.y, updated.z)
                        )
                        is PlayerUpdatedPing -> pings.appendReplacingSameTick(PingSample(tick, updated.ping))
                        is PlayerUpdatedWorldChanged -> worlds.appendReplacingSameTick(WorldSample(tick, updated.wn))
                        else -> Unit
                    }
                }
            }
            return PlayerHistory(
                originEntityId = playerRecord.originEntityId,
                positions = positions.toList(),
                pings = pings.toList(),
                worlds = worlds.toList(),
            )
        }
    }
}

private fun <T : TickSample> previousIndex(samples: List<T>, targetTick: Double): Int {
    val index = samples.binarySearchBy(targetTick) { it.tick.toDouble() }
    return if (index >= 0) index else -index - 2
}

private fun <T : TickSample> MutableList<T>.appendReplacingSameTick(sample: T) {
    if (isNotEmpty() && last().tick == sample.tick) {
        removeAt(lastIndex)
    }
    add(sample)
}
