package net.easecation.ghosty.recording.level.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode

@Serializable
sealed interface LevelUpdated {
    /**
     * @return the type id of this Updated.
     */
    fun getUpdateTypeId(): Int

    /**
     * Process to the playback level.
     * @param node LevelRecordNode
     */
    fun processTo(node: LevelRecordNode) = Unit

    /**
     * Process to the playback level but go backwards.
     * @param node LevelRecordNode
     */
    fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        const val TYPE_BLOCK_CHANGE: Int = 0
        const val TYPE_BLOCK_EVENT: Int = 1
        const val TYPE_LEVEL_EVENT: Int = 2
        const val TYPE_LEVEL_SOUND_EVENT: Int = 3
        const val TYPE_PLAY_SOUND: Int = 4
        const val TYPE_TITLE: Int = 5
        const val TYPE_MESSAGE: Int = 6
        const val TYPE_ACTION_BAR: Int = 7
        const val TYPE_POPUP: Int = 8
        const val TYPE_BOSS_EVENT: Int = 9
        const val TYPE_SCOREBOARD_DISPLAY: Int = 10
        const val TYPE_CUSTOM_EVENT: Int = 11
        const val TYPE_TIME: Int = 12
        const val TYPE_WEATHER: Int = 13
        const val TYPE_OVERLOAD: Int = 14

        /**
         * Create a LevelUpdated from the stream.
         * @param stream BinaryStream
         * @return LevelUpdated
         */
        @JvmStatic
        fun fromBinaryStream(stream: BinaryStream, formatVersion: Int): LevelUpdated {
            return when (val type = stream.byte) {
                TYPE_BLOCK_CHANGE -> LevelUpdatedBlockChange.ADAPTER.read(stream, formatVersion)
                TYPE_BLOCK_EVENT -> LevelUpdatedBlockEvent.ADAPTER.read(stream, formatVersion)
                TYPE_LEVEL_EVENT -> LevelUpdatedLevelEvent.ADAPTER.read(stream, formatVersion)
                TYPE_LEVEL_SOUND_EVENT -> LevelUpdatedLevelSoundEvent.ADAPTER.read(stream, formatVersion)
                TYPE_PLAY_SOUND -> LevelUpdatedPlaySound.ADAPTER.read(stream, formatVersion)
                TYPE_TITLE -> LevelUpdatedTitle.ADAPTER.read(stream, formatVersion)
                TYPE_MESSAGE -> LevelUpdatedMessage.ADAPTER.read(stream, formatVersion)
                TYPE_ACTION_BAR -> LevelUpdatedActionBar.ADAPTER.read(stream, formatVersion)
                TYPE_POPUP -> LevelUpdatedPopup.ADAPTER.read(stream, formatVersion)
                TYPE_CUSTOM_EVENT -> LevelUpdatedCustom.ADAPTER.read(stream, formatVersion)
                TYPE_TIME -> LevelUpdatedTime.ADAPTER.read(stream, formatVersion)
                TYPE_WEATHER -> LevelUpdatedWeather.ADAPTER.read(stream, formatVersion)
                TYPE_OVERLOAD -> LevelUpdatedOverload.ADAPTER.read(stream, formatVersion)
                else -> throw IllegalArgumentException("Unknown LevelUpdated type $type")
            }
        }

        @JvmStatic
        fun writeBinaryStream(updated: LevelUpdated, stream: BinaryStream) {
            return when (updated) {
                is LevelUpdatedBlockChange -> LevelUpdatedBlockChange.ADAPTER.write(updated, stream)
                is LevelUpdatedBlockEvent -> LevelUpdatedBlockEvent.ADAPTER.write(updated, stream)
                is LevelUpdatedLevelEvent -> LevelUpdatedLevelEvent.ADAPTER.write(updated, stream)
                is LevelUpdatedLevelSoundEvent -> LevelUpdatedLevelSoundEvent.ADAPTER.write(updated, stream)
                is LevelUpdatedPlaySound -> LevelUpdatedPlaySound.ADAPTER.write(updated, stream)
                is LevelUpdatedTitle -> LevelUpdatedTitle.ADAPTER.write(updated, stream)
                is LevelUpdatedMessage -> LevelUpdatedMessage.ADAPTER.write(updated, stream)
                is LevelUpdatedActionBar -> LevelUpdatedActionBar.ADAPTER.write(updated, stream)
                is LevelUpdatedPopup -> LevelUpdatedPopup.ADAPTER.write(updated, stream)
                is LevelUpdatedCustom -> LevelUpdatedCustom.ADAPTER.write(updated, stream)
                is LevelUpdatedTime -> LevelUpdatedTime.ADAPTER.write(updated, stream)
                is LevelUpdatedWeather -> LevelUpdatedWeather.ADAPTER.write(updated, stream)
                is LevelUpdatedOverload -> LevelUpdatedOverload.ADAPTER.write(updated, stream)
            }
        }

        @JvmStatic
        fun serializer(updated: LevelUpdated): KSerializer<*> {
            return when (updated) {
                is LevelUpdatedBlockChange -> LevelUpdatedBlockChange.serializer()
                is LevelUpdatedBlockEvent -> LevelUpdatedBlockEvent.serializer()
                is LevelUpdatedLevelEvent -> LevelUpdatedLevelEvent.serializer()
                is LevelUpdatedLevelSoundEvent -> LevelUpdatedLevelSoundEvent.serializer()
                is LevelUpdatedPlaySound -> LevelUpdatedPlaySound.serializer()
                is LevelUpdatedTitle -> LevelUpdatedTitle.serializer()
                is LevelUpdatedMessage -> LevelUpdatedMessage.serializer()
                is LevelUpdatedActionBar -> LevelUpdatedActionBar.serializer()
                is LevelUpdatedPopup -> LevelUpdatedPopup.serializer()
                is LevelUpdatedCustom -> LevelUpdatedCustom.serializer()
                is LevelUpdatedTime -> LevelUpdatedTime.serializer()
                is LevelUpdatedWeather -> LevelUpdatedWeather.serializer()
                is LevelUpdatedOverload -> LevelUpdatedOverload.serializer()
            }
        }
    }
}

interface LevelUpdateAdapter<T : LevelUpdated> {
    fun write(updated: T, stream: BinaryStream)
    fun read(stream: BinaryStream, formatVersion: Int): T
}
