package net.easecation.ghosty.recording.player;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;

/**
 * PlayerRecord 作为玩家录制的数据的载体
 * 提供二进制的写入和读取方法
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:08.
 */
public interface PlayerRecord {

    /**
     * 用于标识PlayerRecord的类型
     */
    byte OBJECT_LML_V0 = 0;
    byte OBJECT_SKINLESS_V0 = 1;
    byte OBJECT_LML_V1 = 2;
    byte OBJECT_SKINLESS_V1 = 3;

    void record(int tick, PlayerRecordNode node);

    PlaybackIterator<PlayerUpdated> iterator();

    String getPlayerName();

    Skin getSkin();

    byte[] toBinary();

    static PlayerRecord fromBinary(byte[] data) {
        BinaryStream stream = new BinaryStream(data);
        byte type = (byte) stream.getByte();
        return switch (type) {
            case OBJECT_SKINLESS_V1 -> new SkinlessPlayerRecord(stream, 1);
            case OBJECT_LML_V1 -> new LmlPlayerRecord(stream, 1);
            case OBJECT_LML_V0 -> new LmlPlayerRecord(stream, 0);
            case OBJECT_SKINLESS_V0 -> new SkinlessPlayerRecord(stream, 0);
            default -> null;
        };
    }
}
