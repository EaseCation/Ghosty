package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:20.
 */
public interface PlayerUpdated {

    int TYPE_POSITION_XYZ = 0;
    int TYPE_ROTATION = 1;
    int TYPE_TAG_NAME = 2;
    int TYPE_WORLD = 3;
    int TYPE_DATA_FLAGS = 4;
    int TYPE_ITEM = 5;
    int TYPE_ARMOR_0 = 6;
    int TYPE_ARMOR_1 = 7;
    int TYPE_ARMOR_2 = 8;
    int TYPE_ARMOR_3 = 9;
    int TYPE_OFFHAND = 10;
    int TYPE_ANIMATE = 11;
    int TYPE_ENTITY_EVENT = 12;

    /**
     * @return the type id of this Updated. 更新类型的ID
     */
    int getUpdateTypeId();

    /**
     * Process to the playback entity. 回放时应用于实体
     * @param ghost Ghost entity
     */
    void processTo(PlaybackNPC ghost);

    /**
     * Apply to the record node. 应用到RecordNode中
     * @param node the node to be applied. 要应用的节点
     * @return self. 返回自身
     */
    PlayerRecordNode applyTo(PlayerRecordNode node);

    /**
     * Write to the stream. 写入到流中
     * @param stream BinaryStream
     */
    void write(BinaryStream stream);

    /**
     * Read from the stream. 从流中读取
     * @param stream BinaryStream
     */
    void read(BinaryStream stream);

    /**
     * Create a PlayerUpdated from the stream. 从流中创建一个PlayerUpdated
     * @param stream BinaryStream
     * @return PlayerUpdated
     */
    static PlayerUpdated fromBinaryStream(BinaryStream stream) {
        return switch (stream.getByte()) {
            case TYPE_POSITION_XYZ -> new PlayerUpdatedPositionXYZ(stream);
            case TYPE_ROTATION -> new PlayerUpdatedRotation(stream);
            case TYPE_TAG_NAME -> new PlayerUpdatedTagName(stream);
            case TYPE_WORLD -> new PlayerUpdatedWorldChanged(stream);
            case TYPE_DATA_FLAGS -> new PlayerUpdatedDataFlags(stream);
            case TYPE_ITEM -> new PlayerUpdatedItem(stream);
            case TYPE_ANIMATE -> new PlayerUpdatedAnimate(stream);
            case TYPE_ENTITY_EVENT -> new PlayerUpdatedEntityEvent(stream);
            case TYPE_ARMOR_0 -> new PlayerUpdatedArmor0(stream);
            case TYPE_ARMOR_1 -> new PlayerUpdatedArmor1(stream);
            case TYPE_ARMOR_2 -> new PlayerUpdatedArmor2(stream);
            case TYPE_ARMOR_3 -> new PlayerUpdatedArmor3(stream);
            case TYPE_OFFHAND -> new PlayerUpdatedOffhand(stream);
            default -> null;
        };
    }

}
