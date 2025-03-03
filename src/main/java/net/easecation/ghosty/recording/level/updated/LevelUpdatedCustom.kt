package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode
import net.easecation.ghosty.serializer.GsonObjectSerializer

@Serializable
data class LevelUpdatedCustom(
    @Serializable(with = GsonObjectSerializer::class)
    val obj: JsonObject
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_CUSTOM_EVENT

    override fun processTo(node: LevelRecordNode) = Unit

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        private val GSON = Gson()

        @JvmStatic
        fun of(obj: JsonObject) = LevelUpdatedCustom(obj)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedCustom> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedCustom> {
        override fun write(updated: LevelUpdatedCustom, stream: BinaryStream) {
            stream.putString(updated.obj.toString())
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedCustom {
            return LevelUpdatedCustom(GSON.fromJson(stream.string, JsonObject::class.java))
        }
    }
}
