package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:20.
 */
public interface EntityUpdated {

    int TYPE_POSITION_XYZ = 0;
    int TYPE_ROTATION = 1;
    int TYPE_TAG_NAME = 2;
    int TYPE_SCORE_TAG = 3;
    int TYPE_DATA_FLAGS = 4;
    int TYPE_ITEM = 5;
    int TYPE_SCALE = 6;
    int TYPE_CLOSE = 7;
    int TYPE_NAMETAG_ALWAYS_VISIBLE = 8;
    int TYPE_SKIN_ID = 9;
    int TYPE_NPC_SKIN_ID = 10;
    int TYPE_VARIANT = 11;
    int TYPE_MARK_VARIANT = 12;
    int TYPE_SKIN_INFO = 13;
    int MAX_TYPE_ID = 13;

    /**
     * @return the type id of this Updated. 更新类型的ID
     */
    int getUpdateTypeId();

    /**
     * 如果有状态，则表示需要在回退时寻找更早之前的该状态，进行应用
     * @return whether this Updated has states. 是否有状态
     */
    boolean hasStates();

    /**
     * Process to the playback entity. 回放时应用于实体
     * @param entity Simulated entity
     */
    void processTo(SimulatedEntity entity);

    /**
     * Apply to the record node. 应用到RecordNode中
     * @param node the node to be applied. 要应用的节点
     * @return self. 返回自身
     */
    EntityRecordNode applyTo(EntityRecordNode node);

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
    static EntityUpdated fromBinaryStream(BinaryStream stream) {
        return switch (stream.getByte()) {
            case TYPE_POSITION_XYZ -> new EntityUpdatedPositionXYZ(stream);
            case TYPE_ROTATION -> new EntityUpdatedRotation(stream);
            case TYPE_TAG_NAME -> new EntityUpdatedTagName(stream);
            case TYPE_SCORE_TAG -> new EntityUpdatedScoreTag(stream);
            case TYPE_DATA_FLAGS -> new EntityUpdatedDataFlags(stream);
            case TYPE_ITEM -> new EntityUpdatedItem(stream);
            case TYPE_SCALE -> new EntityUpdatedScale(stream);
            case TYPE_CLOSE -> new EntityUpdatedClose(stream);
            case TYPE_NAMETAG_ALWAYS_VISIBLE -> new EntityUpdatedNameTagAlwaysVisible(stream);
            case TYPE_SKIN_ID -> new EntityUpdatedSkinId(stream);
            case TYPE_NPC_SKIN_ID -> new EntityUpdatedNPCSkinId(stream);
            case TYPE_VARIANT -> new EntityUpdatedVariant(stream);
            case TYPE_MARK_VARIANT -> new EntityUpdatedMarkVariant(stream);
            case TYPE_SKIN_INFO -> new EntityUpdatedSkinInfo(stream);
            default -> null;
        };
    }

}
