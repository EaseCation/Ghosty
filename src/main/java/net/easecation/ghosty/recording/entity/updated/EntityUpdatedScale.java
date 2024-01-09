package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:26.
 */
public class EntityUpdatedScale implements EntityUpdated {

    private float scale;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setScale(scale);
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setScale(scale);
        return node;
    }

    public static EntityUpdatedScale of(float scale) {
        return new EntityUpdatedScale(scale);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_SCALE;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public EntityUpdatedScale(BinaryStream stream) {
        this.read(stream);
    }

    private EntityUpdatedScale(float scale) {
        this.scale = scale;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedScale o)) return false;
        return (scale == o.scale);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putFloat((float) this.scale);
    }

    @Override
    public void read(BinaryStream stream) {
        this.scale = stream.getFloat();
    }

    @Override
    public String toString() {
        return "EntityUpdatedScale{" +
            "scale=" + scale +
            '}';
    }

}
