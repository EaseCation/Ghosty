package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedActionBar(
    var message: String,
    var fadeInTime: Int,
    var stayTime: Int,
    var fadeOutTime: Int,
) : LevelUpdated {
    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_ACTION_BAR

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { level ->
            level.players.values.forEach { player ->
                player.sendActionBar(message, fadeInTime, stayTime, fadeOutTime)
            }
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(message: String, fadeInTime: Int, stayTime: Int, fadeOutTime: Int) =
            LevelUpdatedActionBar(message, fadeInTime, stayTime, fadeOutTime)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedActionBar> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedActionBar> {
        override fun write(updated: LevelUpdatedActionBar, stream: BinaryStream) {
            stream.putString(updated.message)
            stream.putLInt(updated.fadeInTime)
            stream.putLInt(updated.stayTime)
            stream.putLInt(updated.fadeOutTime)
        }

        override fun read(stream: BinaryStream, formatVersion: Int) = LevelUpdatedActionBar(
            message = stream.string,
            fadeInTime = stream.lInt,
            stayTime = stream.lInt,
            fadeOutTime = stream.lInt,
        )
    }
}
