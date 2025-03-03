package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedOverload(
    val tickUseMillis: Int
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_OVERLOAD

    override fun processTo(node: LevelRecordNode) = Unit

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(tickUseMillis: Int) = LevelUpdatedOverload(tickUseMillis)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedOverload> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedOverload> {
        override fun write(updated: LevelUpdatedOverload, stream: BinaryStream) {
            stream.putUnsignedVarInt(updated.tickUseMillis.toLong())
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedOverload {
            return LevelUpdatedOverload(stream.unsignedVarInt.toInt())
        }
    }
}
