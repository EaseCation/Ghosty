package net.easecation.ghosty.recording.entity;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.entity.updated.EntityUpdated;

/**
 * PlayerRecord 作为玩家录制的数据的载体
 * 提供二进制的写入和读取方法
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:08.
 */
public interface EntityRecord {

    /**
     * 用于标识PlayerRecord的类型
     */
    byte OBJECT_V0 = 0;

    void record(int tick, EntityRecordNode node);

    void recordClose(int tick);

    PlaybackIterator<EntityUpdated> iterator();

    long getEntityId();

    int getNetworkId();

    /**
     * 如果是自定义实体（networkId为0），则需要返回自定义实体的类型标识符。否则返回空字符串
     * @return 自定义实体的类型标识符
     */
    String getEntityIdentifier();

    byte[] toBinary();

    static EntityRecord fromBinary(byte[] data) {
        BinaryStream stream = new BinaryStream(data);
        byte type = (byte) stream.getByte();
        return switch (type) {
            case OBJECT_V0 -> new EntityRecordImpl(stream);
            default -> null;
        };
    }
}
