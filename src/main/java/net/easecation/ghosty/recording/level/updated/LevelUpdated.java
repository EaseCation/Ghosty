package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public interface LevelUpdated {

    int TYPE_BLOCK_CHANGE = 0;
    int TYPE_LEVEL_EVENT = 1;
    int TYPE_LEVEL_SOUND_EVENT = 2;
    int TYPE_PLAY_SOUND = 3;

    /**
     * @return the type id of this Updated.
     */
    int getUpdateTypeId();

    /**
     * Process to the playback level.
     * @param node LevelRecordNode
     */
    void processTo(LevelRecordNode node);

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
            case TYPE_LEVEL_EVENT -> new LevelUpdatedLevelEvent(stream);
            case TYPE_LEVEL_SOUND_EVENT -> new LevelUpdatedLevelSoundEvent(stream);
            case TYPE_PLAY_SOUND -> new LevelUpdatedPlaySound(stream);
            default -> throw new IllegalArgumentException("Unknown LevelUpdated type id: " + type);
        };
    }

}
