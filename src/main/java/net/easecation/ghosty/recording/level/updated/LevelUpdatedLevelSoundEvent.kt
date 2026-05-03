package net.easecation.ghosty.recording.level.updated

import cn.nukkit.level.Level
import cn.nukkit.math.Vector3f
import cn.nukkit.network.protocol.LevelSoundEventPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode
import net.easecation.ghosty.serializer.NukkitVector3fSerializer
import org.itxtech.synapseapi.multiprotocol.protocol12170.protocol.LevelSoundEventPacketV312170
import org.itxtech.synapseapi.multiprotocol.protocol12620.protocol.LevelSoundEventPacketV312620
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
    val entityUniqueId: Long = -1,
    @Serializable(with = NukkitVector3fSerializer::class)
    val fireAtPosition: Vector3f? = null,
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
        fun of(packet: LevelSoundEventPacketV312170): LevelUpdatedLevelSoundEvent {
            return LevelUpdatedLevelSoundEvent(
                packet.sound,
                packet.x,
                packet.y,
                packet.z,
                packet.extraData,
                packet.entityIdentifier,
                packet.isBabyMob,
                packet.isGlobal,
                packet.entityUniqueId
            )
        }

        @JvmStatic
        fun of(packet: LevelSoundEventPacketV312620): LevelUpdatedLevelSoundEvent {
            return LevelUpdatedLevelSoundEvent(
                packet.sound,
                packet.x,
                packet.y,
                packet.z,
                packet.extraData,
                packet.entityIdentifier,
                packet.isBabyMob,
                packet.isGlobal,
                packet.entityUniqueId,
                packet.fireAtPosition
            )
        }


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
            stream.putLLong(updated.entityUniqueId)
            stream.putOptional(updated.fireAtPosition, BinaryStream::putVector3f)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedLevelSoundEvent {
            val sound = stream.unsignedVarInt.toInt()
            val vector = stream.vector3f
            val extraData = stream.varInt
            val entityIdentifier = stream.string
            val isBabyMob = stream.boolean
            val isGlobal = stream.boolean
            val entityUniqueId: Long
            val fireAtPosition: Vector3f?
            when (formatVersion) {
                4 -> {
                    entityUniqueId = stream.lLong
                    fireAtPosition = stream.getOptional(BinaryStream::getVector3f)
                }

                3 -> {
                    entityUniqueId = stream.lLong
                    fireAtPosition = null
                }

                0, 1, 2 -> {
                    entityUniqueId = -1
                    fireAtPosition = null
                }

                else -> throw IllegalArgumentException("Unsupported format version: $formatVersion")
            }
            return LevelUpdatedLevelSoundEvent(
                sound,
                vector.x,
                vector.y,
                vector.z,
                extraData,
                entityIdentifier,
                isBabyMob,
                isGlobal,
                entityUniqueId,
                fireAtPosition,
            )
        }
    }
}