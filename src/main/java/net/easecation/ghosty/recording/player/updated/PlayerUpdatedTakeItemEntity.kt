package net.easecation.ghosty.recording.player.updated

import cn.nukkit.Server
import cn.nukkit.entity.Entity
import cn.nukkit.network.protocol.TakeItemEntityPacket
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
data class PlayerUpdatedTakeItemEntity(
    val targetEntityId: Long
) : PlayerUpdated {

    override fun getUpdateTypeId() = PlayerUpdated.TYPE_TAKE_ITEM_ENTITY

    override fun processTo(ghost: PlaybackNPC) {
        val targetId = ghost.level?.entities?.filterIsInstance<SimulatedEntity>()
            ?.firstOrNull { it.originEid == targetEntityId }
            ?.id ?: return

        val pkt = TakeItemEntityPacket().apply {
            entityId = ghost.id
            target = targetId
        }
        Server.broadcastPacket(ghost.viewers.values, pkt)
    }

    override fun applyTo(node: PlayerRecordNode) = node

    companion object {
        @JvmStatic
        fun of(targetEntityId: Long) = PlayerUpdatedTakeItemEntity(targetEntityId)

        @JvmStatic
        val ADAPTER: PlayerUpdateAdapter<PlayerUpdatedTakeItemEntity> = Adapter
    }

    private object Adapter : PlayerUpdateAdapter<PlayerUpdatedTakeItemEntity> {
        override fun write(updated: PlayerUpdatedTakeItemEntity, stream: BinaryStream) {
            stream.putEntityUniqueId(updated.targetEntityId)
        }

        override fun read(stream: BinaryStream): PlayerUpdatedTakeItemEntity {
            return PlayerUpdatedTakeItemEntity(stream.entityUniqueId)
        }
    }
}