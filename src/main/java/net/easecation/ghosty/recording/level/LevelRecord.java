package net.easecation.ghosty.recording.level;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;

public interface LevelRecord {

    byte VERSION_0 = 0;
    byte VERSION_1 = 1;
    byte VERSION_2 = 2;

    void record(int tick, LevelRecordNode node);

    PlaybackIterator<LevelUpdated> iterator();

    int getBaseGameVersionProtocol();

    byte[] toBinary();

    static LevelRecord fromBinary(byte[] data) {
        BinaryStream stream = new BinaryStream(data);
        byte type = (byte) stream.getByte();
        return switch (type) {
            case VERSION_2 -> new LevelRecordImpl(stream, 2);
            case VERSION_1 -> new LevelRecordImpl(stream, 1);
            case VERSION_0 -> new LevelRecordImpl(stream, 0);
            default -> null;
        };
    }

}
