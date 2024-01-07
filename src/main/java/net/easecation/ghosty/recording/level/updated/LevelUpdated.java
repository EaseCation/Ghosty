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
    static LevelUpdated fromBinaryStream(BinaryStream stream) {
        int type = stream.getByte();
        return switch (type) {
            case TYPE_BLOCK_CHANGE -> new LevelUpdatedBlockChange(stream);
            case TYPE_BLOCK_EVENT -> new LevelUpdatedBlockEvent(stream);
            case TYPE_LEVEL_EVENT -> new LevelUpdatedLevelEvent(stream);
            case TYPE_LEVEL_SOUND_EVENT -> new LevelUpdatedLevelSoundEvent(stream);
            case TYPE_PLAY_SOUND -> new LevelUpdatedPlaySound(stream);
            case TYPE_TITLE -> new LevelUpdatedTitle(stream);
            case TYPE_MESSAGE -> new LevelUpdatedMessage(stream);
            default -> throw new IllegalArgumentException("Unknown LevelUpdated type id: " + type);
        };
    }

}
