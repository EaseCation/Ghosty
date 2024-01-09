package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.IntEntityData;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:26.
 */
public class EntityUpdatedMarkVariant implements EntityUpdated {

    private int data;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setDataProperty(new IntEntityData(Entity.DATA_MARK_VARIANT, this.data));
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setSkinId(data);
        return node;
    }

    public static EntityUpdatedMarkVariant of(int data) {
        return new EntityUpdatedMarkVariant(data);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_MARK_VARIANT;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public EntityUpdatedMarkVariant(BinaryStream stream) {
        this.read(stream);
    }

    private EntityUpdatedMarkVariant(int data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedMarkVariant o)) return false;
        return data == o.data;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putVarInt(this.data);
    }

    @Override
    public void read(BinaryStream stream) {
        this.data = stream.getVarInt();
    }

    @Override
    public String toString() {
        return "EntityUpdatedMarkVariant{" +
            "data=" + data +
            '}';
    }
}
