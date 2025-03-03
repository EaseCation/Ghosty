package net.easecation.ghosty.recording.level.updated

import cn.nukkit.level.Level
import cn.nukkit.math.Vector3f
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LevelSoundEventPacket14
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.LevelSoundEventPacket16
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacket18
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacketV218
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.LevelSoundEventPacketV319

@Serializable
data class LevelUpdatedLevelSoundEvent(
    val sound: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val extraData: Int,
    val entityIdentifier: String,
    val isBabyMob: Boolean,
    val isGlobal: Boolean,
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_LEVEL_SOUND_EVENT

    override fun processTo(node: LevelRecordNode) {
        LevelSoundEventPacket().also {
            it.sound = sound
            it.x = x
            it.y = y
            it.z = z
            it.extraData = extraData
            it.entityIdentifier = entityIdentifier
            it.isBabyMob = isBabyMob
            it.isGlobal = isGlobal
        }.also { packet ->
            node.handleLevelChunkPacket(Level.chunkHash((x.toInt() shr 4), (z.toInt() shr 4)), packet)
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(packet: LevelSoundEventPacket) =
            LevelUpdatedLevelSoundEvent(
                packet.sound, packet.x, packet.y, packet.z,
                packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal
            )

        @JvmStatic
        fun of(packet: LevelSoundEventPacket14) = LevelUpdatedLevelSoundEvent(
            packet.sound, packet.x, packet.y, packet.z,
            packet.extraData, ":", packet.isBabyMob, packet.isGlobal
        )

        @JvmStatic
        fun of(packet: LevelSoundEventPacket16) =
            LevelUpdatedLevelSoundEvent(
                packet.sound, packet.x, packet.y, packet.z,
                packet.extraData, ":", packet.isBabyMob, packet.isGlobal
            )

        @JvmStatic
        fun of(packet: LevelSoundEventPacket18) =
            LevelUpdatedLevelSoundEvent(
                packet.sound, packet.x, packet.y, packet.z,
                packet.extraData, ":", packet.isBabyMob, packet.isGlobal
            )

        @JvmStatic
        fun of(packet: LevelSoundEventPacketV218) =
            LevelUpdatedLevelSoundEvent(
                packet.sound, packet.x, packet.y, packet.z,
                packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal
            )

        @JvmStatic
        fun of(packet: LevelSoundEventPacketV319) =
            LevelUpdatedLevelSoundEvent(
                packet.sound, packet.x, packet.y, packet.z,
                packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal
            )

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedLevelSoundEvent> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedLevelSoundEvent> {
        override fun write(updated: LevelUpdatedLevelSoundEvent, stream: BinaryStream) {
            stream.putUnsignedVarInt(updated.sound.toLong())
            stream.putVector3f(updated.x, updated.y, updated.z)
            stream.putVarInt(updated.extraData)
            stream.putString(updated.entityIdentifier)
            stream.putBoolean(updated.isBabyMob)
            stream.putBoolean(updated.isGlobal)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedLevelSoundEvent {
            val sound = stream.unsignedVarInt.toInt()
            val vector = stream.vector3f
            val extraData = stream.varInt
            val entityIdentifier = stream.string
            val isBabyMob = stream.boolean
            val isGlobal = stream.boolean
            return LevelUpdatedLevelSoundEvent(sound, vector.x, vector.y, vector.z, extraData, entityIdentifier, isBabyMob, isGlobal)
        }
    }
}