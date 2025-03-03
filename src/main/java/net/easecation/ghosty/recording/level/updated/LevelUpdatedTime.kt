package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedTime(
    val time: Int
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_TIME

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { it.time = time }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(time: Int) = LevelUpdatedTime(time)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedTime> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedTime> {
        override fun write(updated: LevelUpdatedTime, stream: BinaryStream) {
            stream.putVarInt(updated.time)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedTime {
            return LevelUpdatedTime(stream.varInt)
        }
    }
}
