package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedPopup(
    val message: String
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_POPUP

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { level ->
            level.players.values.forEach { player ->
                player.sendPopup(message)
            }
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(message: String) = LevelUpdatedPopup(message)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedPopup> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedPopup> {
        override fun write(updated: LevelUpdatedPopup, stream: BinaryStream) {
            stream.putString(updated.message)
        }

        override fun read(stream: BinaryStream, formatVersion: Int) = LevelUpdatedPopup(
            message = stream.string
        )
    }
}