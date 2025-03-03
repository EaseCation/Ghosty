package net.easecation.ghosty.recording.level.updated

import cn.nukkit.level.Level
import cn.nukkit.math.BlockVector3
import cn.nukkit.network.protocol.PlaySoundPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedPlaySound(
    val name: String,
    val x: Int,
    val y: Int,
    val z: Int,
    val volume: Float,
    val pitch: Float
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_PLAY_SOUND

    override fun processTo(node: LevelRecordNode) {
        PlaySoundPacket().also {
            it.name = name
            it.x = x
            it.y = y
            it.z = z
            it.volume = volume
            it.pitch = pitch
        }.also { packet ->
            node.handleLevelChunkPacket(Level.chunkHash(x shr 4, z shr 4), packet)
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(packet: PlaySoundPacket) =
            LevelUpdatedPlaySound(packet.name, packet.x, packet.y, packet.z, packet.volume, packet.pitch)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedPlaySound> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedPlaySound> {
        override fun write(updated: LevelUpdatedPlaySound, stream: BinaryStream) {
            stream.putString(updated.name)
            stream.putBlockVector3(BlockVector3(updated.x, updated.y, updated.z))
            stream.putLFloat(updated.volume)
            stream.putLFloat(updated.pitch)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedPlaySound {
            val name = stream.string
            val pos = stream.blockVector3
            val volume = stream.lFloat
            val pitch = stream.lFloat
            return LevelUpdatedPlaySound(name, pos.x, pos.y, pos.z, volume, pitch)
        }
    }
}