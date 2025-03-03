package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedMessage(
    var message: String
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_MESSAGE

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { level ->
            level.players.values.forEach { player ->
                player.sendMessage(message)
            }
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(message: String) = LevelUpdatedMessage(message)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedMessage> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedMessage> {
        override fun write(updated: LevelUpdatedMessage, stream: BinaryStream) {
            stream.putString(updated.message)
        }

        override fun read(stream: BinaryStream, formatVersion: Int) = LevelUpdatedMessage(
            message = stream.string
        )
    }
}
