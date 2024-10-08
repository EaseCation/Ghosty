package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public interface LevelUpdated {

    int TYPE_BLOCK_CHANGE = 0;
    int TYPE_BLOCK_EVENT = 1;
    int TYPE_LEVEL_EVENT = 2;
    int TYPE_LEVEL_SOUND_EVENT = 3;
    int TYPE_PLAY_SOUND = 4;
    int TYPE_TITLE = 5;
    int TYPE_MESSAGE = 6;
    int TYPE_ACTION_BAR = 7;
    int TYPE_POPUP = 8;
    int TYPE_BOSS_EVENT = 9;
    int TYPE_SCOREBOARD_DISPLAY = 10;
    int TYPE_CUSTOM_EVENT = 11;
    int TYPE_TIME = 12;
    int TYPE_WEATHER = 13;
    int TYPE_OVERLOAD = 14;

    /**
     * @return the type id of this Updated.
     */
    int getUpdateTypeId();

    /**
     * Process to the playback level.
     * @param node LevelRecordNode
     */
    void processTo(LevelRecordNode node);

    void backwardTo(LevelRecordNode node);

    /**
     * Write to the stream.
     * @param stream BinaryStream
     */
    void write(BinaryStream stream);

    /**
     * Read from the stream.
     * @param stream BinaryStream
     */
    void read(BinaryStream stream);

    /**
     * Create a LevelUpdated from the stream.
     * @param stream BinaryStream
     * @return LevelUpdated
     */
    static LevelUpdated fromBinaryStream(BinaryStream stream, int formatVersion) {
        int type = stream.getByte();
        return switch (type) {
            case TYPE_BLOCK_CHANGE -> new LevelUpdatedBlockChange(stream, formatVersion);
            case TYPE_BLOCK_EVENT -> new LevelUpdatedBlockEvent(stream);
            case TYPE_LEVEL_EVENT -> new LevelUpdatedLevelEvent(stream); //TODO: use persistence data instead of runtime data
            case TYPE_LEVEL_SOUND_EVENT -> new LevelUpdatedLevelSoundEvent(stream); //TODO: use persistence data instead of runtime data
            case TYPE_PLAY_SOUND -> new LevelUpdatedPlaySound(stream);
            case TYPE_TITLE -> new LevelUpdatedTitle(stream);
            case TYPE_MESSAGE -> new LevelUpdatedMessage(stream);
            case TYPE_ACTION_BAR -> new LevelUpdatedActionBar(stream);
            case TYPE_POPUP -> new LevelUpdatedPopup(stream);
            // case TYPE_BOSS_EVENT -> new LevelUpdatedBossEvent(stream);
            // case TYPE_SCOREBOARD_DISPLAY -> new LevelUpdatedScoreboardDisplay(stream);
            case TYPE_CUSTOM_EVENT -> new LevelUpdatedCustom(stream);
            case TYPE_TIME -> new LevelUpdatedTime(stream);
            case TYPE_WEATHER -> new LevelUpdatedWeather(stream);
            case TYPE_OVERLOAD -> new LevelUpdatedOverload(stream);
            default -> throw new IllegalArgumentException("Unknown LevelUpdated type id: " + type);
        };
    }

}
