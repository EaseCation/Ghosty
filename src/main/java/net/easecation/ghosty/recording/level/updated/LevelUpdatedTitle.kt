package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedTitle(
    var title: String,
    var subTitle: String,
    var fadeInTime: Int,
    var stayTime: Int,
    var fadeOutTime: Int
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_TITLE

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { level ->
            level.players.values.forEach { player ->
                player.sendTitle(title, subTitle, fadeInTime, stayTime, fadeOutTime)
            }
        }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(title: String, subTitle: String, fadeInTime: Int, stayTime: Int, fadeOutTime: Int) =
            LevelUpdatedTitle(title, subTitle, fadeInTime, stayTime, fadeOutTime)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedTitle> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedTitle> {
        override fun write(updated: LevelUpdatedTitle, stream: BinaryStream) {
            stream.putString(updated.title)
            stream.putString(updated.subTitle)
            stream.putLInt(updated.fadeInTime)
            stream.putLInt(updated.stayTime)
            stream.putLInt(updated.fadeOutTime)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedTitle {
            return LevelUpdatedTitle(
                title = stream.string,
                subTitle = stream.string,
                fadeInTime = stream.lInt,
                stayTime = stream.lInt,
                fadeOutTime = stream.lInt
            )
        }
    }
}