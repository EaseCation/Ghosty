package net.easecation.ghosty.recording.level.updated

import cn.nukkit.level.Level
import cn.nukkit.math.BlockVector3
import cn.nukkit.network.protocol.BlockEventPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedBlockEvent(
    val x: Int,
    val y: Int,
    val z: Int,
    val eventType: Int,
    val eventData: Int,
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_BLOCK_EVENT

    override fun processTo(node: LevelRecordNode) {
        BlockEventPacket().also {
            it.x = x
            it.y = y
            it.z = z
            it.eventType = eventType
            it.eventData = eventData
        }.also { packet ->
            node.handleLevelChunkPacket(Level.chunkHash(x shr 4, z shr 4), packet)
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(packet: BlockEventPacket) =
            LevelUpdatedBlockEvent(packet.eventType, packet.x, packet.y, packet.z, packet.eventData)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedBlockEvent> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedBlockEvent> {
        override fun write(updated: LevelUpdatedBlockEvent, stream: BinaryStream) {
            stream.putBlockVector3(updated.x, updated.y, updated.z)
            stream.putVarInt(updated.eventType)
            stream.putVarInt(updated.eventData)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedBlockEvent {
            val pos = stream.blockVector3
            return LevelUpdatedBlockEvent(
                x = pos.x,
                y = pos.y,
                z = pos.z,
                eventType = stream.varInt,
                eventData = stream.varInt
            )
        }
    }
}
