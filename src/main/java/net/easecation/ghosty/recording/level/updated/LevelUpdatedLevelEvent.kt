package net.easecation.ghosty.recording.level.updated

import cn.nukkit.level.Level
import cn.nukkit.math.Vector3f
import cn.nukkit.network.protocol.LevelEventPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.LevelEventPacket112
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.LevelEventPacket116100
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LevelEventPacket14
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.LevelEventPacket16
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.LevelEventPacket17

@Serializable
data class LevelUpdatedLevelEvent(
    val evid: Int,
    val x: Float,
    val y: Float,
    val z: Float,
    val data: Int,
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_LEVEL_EVENT

    override fun processTo(node: LevelRecordNode) {
        LevelEventPacket().also {
            it.evid = this.evid
            it.x = this.x
            it.y = this.y
            it.z = this.z
            it.data = this.data
        }.also { packet ->
            node.handleLevelChunkPacket(Level.chunkHash((x.toInt() shr 4), (z.toInt() shr 4)), packet)
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(packet: LevelEventPacket) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        fun of(packet: LevelEventPacket14) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        fun of(packet: LevelEventPacket16) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        fun of(packet: LevelEventPacket17) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        fun of(packet: LevelEventPacket112) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        fun of(packet: LevelEventPacket116100) =
            LevelUpdatedLevelEvent(packet.evid, packet.x, packet.y, packet.z, packet.data)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedLevelEvent> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedLevelEvent> {
        override fun write(updated: LevelUpdatedLevelEvent, stream: BinaryStream) {
            stream.putVarInt(updated.evid)
            stream.putVector3f(updated.x, updated.y, updated.z)
            stream.putVarInt(updated.data)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedLevelEvent {
            val evid = stream.varInt
            val vector3f = stream.vector3f
            val data = stream.varInt
            return LevelUpdatedLevelEvent(evid, vector3f.x, vector3f.y, vector3f.z, data)
        }
    }
}