package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:26.
 */
public class EntityUpdatedNameTagAlwaysVisible implements EntityUpdated {

    private boolean visible;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setNameTagAlwaysVisible(visible);
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setNameTagAlwaysVisible(visible);
        return node;
    }

    public static EntityUpdatedNameTagAlwaysVisible of(boolean visible) {
        return new EntityUpdatedNameTagAlwaysVisible(visible);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_NAMETAG_ALWAYS_VISIBLE;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public EntityUpdatedNameTagAlwaysVisible(BinaryStream stream) {
        this.read(stream);
    }

    private EntityUpdatedNameTagAlwaysVisible(boolean visible) {
        this.visible = visible;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedNameTagAlwaysVisible o)) return false;
        return (visible == o.visible);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putByte((byte) (this.visible ? 1 : 0));
    }

    @Override
    public void read(BinaryStream stream) {
        this.visible = stream.getBoolean();
    }

    @Override
    public String toString() {
        return "EntityUpdatedNameTagAlwaysVisible{" +
            "visible=" + visible +
            '}';
    }
}
