package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
data class LevelUpdatedWeather(
    val rain: Boolean,
    val intensity: Int
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_WEATHER

    override fun processTo(node: LevelRecordNode) {
        node.offerLevelGlobalCallback { it.setRaining(rain, intensity) }
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(rain: Boolean, intensity: Int) = LevelUpdatedWeather(rain, intensity)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedWeather> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedWeather> {
        override fun write(updated: LevelUpdatedWeather, stream: BinaryStream) {
            stream.putBoolean(updated.rain)
            stream.putVarInt(updated.intensity)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedWeather {
            return LevelUpdatedWeather(
                rain = stream.boolean,
                intensity = stream.varInt
            )
        }
    }
}