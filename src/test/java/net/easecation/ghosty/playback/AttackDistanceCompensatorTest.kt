package net.easecation.ghosty.playback

import cn.nukkit.entity.data.Skin
import net.easecation.ghosty.PlaybackIterator
import net.easecation.ghosty.recording.player.PlayerRecord
import net.easecation.ghosty.recording.player.PlayerRecordNode
import net.easecation.ghosty.recording.player.updated.PlayerUpdated
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPing
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPositionXYZ
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedWorldChanged
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class AttackDistanceCompensatorTest {

    @Test
    fun `compensates target position by attacker ping`() {
        val attacker = record(
            entityId = 1,
            world(0, "world"),
            ping(0, 200),
            position(0, 0.0),
            position(10, 4.0),
        )
        val victim = record(
            entityId = 2,
            world(0, "world"),
            position(0, 4.0),
            position(10, 8.0),
        )
        val result = AttackDistanceCompensator.fromPlayerRecords(listOf(attacker, victim))
            .calculate(attacker, victim.originEntityId, 10)

        val available = assertIs<AttackDistanceCompensationResult.Available>(result)
        assertEquals(200, available.attackerPingMs)
        assertEquals(6.0, available.visualTargetTick, 0.0001)
        assertEquals(2.4, available.distance, 0.0001)
    }

    @Test
    fun `interpolates victim position on fractional visual tick`() {
        val attacker = record(
            entityId = 1,
            ping(0, 125),
            position(10, 10.0),
        )
        val victim = record(
            entityId = 2,
            position(0, 0.0),
            position(10, 10.0),
        )
        val result = AttackDistanceCompensator.fromPlayerRecords(listOf(attacker, victim))
            .calculate(attacker, victim.originEntityId, 10)

        val available = assertIs<AttackDistanceCompensationResult.Available>(result)
        assertEquals(7.5, available.visualTargetTick, 0.0001)
        assertEquals(2.5, available.distance, 0.0001)
    }

    @Test
    fun `returns unavailable when ping is missing or invalid`() {
        val noPingAttacker = record(
            entityId = 1,
            position(10, 0.0),
        )
        val invalidPingAttacker = record(
            entityId = 2,
            ping(0, -1),
            position(10, 0.0),
        )
        val victim = record(
            entityId = 3,
            position(0, 0.0),
            position(10, 1.0),
        )
        val compensator = AttackDistanceCompensator.fromPlayerRecords(listOf(noPingAttacker, invalidPingAttacker, victim))

        assertUnavailable(
            AttackDistanceCompensationUnavailableReason.ATTACKER_PING_UNAVAILABLE,
            compensator.calculate(noPingAttacker, victim.originEntityId, 10),
        )
        assertUnavailable(
            AttackDistanceCompensationUnavailableReason.ATTACKER_PING_UNAVAILABLE,
            compensator.calculate(invalidPingAttacker, victim.originEntityId, 10),
        )
    }

    @Test
    fun `returns unavailable when target is missing or has no historical position`() {
        val attacker = record(
            entityId = 1,
            ping(0, 200),
            position(2, 0.0),
        )
        val victim = record(
            entityId = 2,
            position(1, 1.0),
        )
        val compensator = AttackDistanceCompensator.fromPlayerRecords(listOf(attacker, victim))

        assertUnavailable(
            AttackDistanceCompensationUnavailableReason.TARGET_NOT_FOUND,
            compensator.calculate(attacker, 99, 2),
        )
        assertUnavailable(
            AttackDistanceCompensationUnavailableReason.TARGET_POSITION_UNAVAILABLE,
            compensator.calculate(attacker, victim.originEntityId, 2),
        )
    }

    @Test
    fun `does not interpolate across long position gaps`() {
        val attacker = record(
            entityId = 1,
            ping(0, 250),
            position(15, 5.0),
        )
        val victim = record(
            entityId = 2,
            position(0, 0.0),
            position(30, 30.0),
        )
        val result = AttackDistanceCompensator.fromPlayerRecords(listOf(attacker, victim))
            .calculate(attacker, victim.originEntityId, 15)

        val available = assertIs<AttackDistanceCompensationResult.Available>(result)
        assertEquals(10.0, available.visualTargetTick, 0.0001)
        assertEquals(5.0, available.distance, 0.0001)
    }

    @Test
    fun `returns unavailable when sampled worlds mismatch`() {
        val attacker = record(
            entityId = 1,
            world(0, "attacker-world"),
            ping(0, 0),
            position(10, 0.0),
        )
        val victim = record(
            entityId = 2,
            world(0, "victim-world"),
            position(10, 1.0),
        )
        val result = AttackDistanceCompensator.fromPlayerRecords(listOf(attacker, victim))
            .calculate(attacker, victim.originEntityId, 10)

        assertUnavailable(AttackDistanceCompensationUnavailableReason.WORLD_MISMATCH, result)
    }

    private fun assertUnavailable(
        reason: AttackDistanceCompensationUnavailableReason,
        result: AttackDistanceCompensationResult,
    ) {
        val unavailable = assertIs<AttackDistanceCompensationResult.Unavailable>(result)
        assertEquals(reason, unavailable.reason)
    }

    private fun record(
        entityId: Long,
        vararg updates: Pair<Int, PlayerUpdated>,
    ): TestPlayerRecord {
        return TestPlayerRecord(entityId, "player-$entityId", updates.toList())
    }

    private fun position(tick: Int, x: Double): Pair<Int, PlayerUpdated> {
        return tick to PlayerUpdatedPositionXYZ.of(x, 0.0, 0.0)
    }

    private fun ping(tick: Int, ping: Int): Pair<Int, PlayerUpdated> {
        return tick to PlayerUpdatedPing.of(ping)
    }

    private fun world(tick: Int, worldName: String): Pair<Int, PlayerUpdated> {
        return tick to PlayerUpdatedWorldChanged.of(worldName)
    }

    private class TestPlayerRecord(
        private val entityId: Long,
        private val name: String,
        private val updates: List<Pair<Int, PlayerUpdated>>,
    ) : PlayerRecord {

        override fun record(tick: Int, node: PlayerRecordNode) = Unit

        override fun getRecDataUnsafe(): List<PlayerUpdated> {
            return updates.map { it.second }
        }

        override fun iterator(): PlaybackIterator<PlayerUpdated> {
            val iterator = PlaybackIterator<PlayerUpdated>()
            updates.forEach { (tick, updated) ->
                iterator.insert(tick, updated)
            }
            return iterator
        }

        override fun getProtocol(): Int {
            return 0
        }

        override fun getPlayerName(): String {
            return name
        }

        override fun getOriginEntityId(): Long {
            return entityId
        }

        override fun getSkin(): Skin {
            return Skin()
        }

        override fun toBinary(): ByteArray {
            return ByteArray(0)
        }
    }
}
