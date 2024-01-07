package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class EntityUpdatedClose implements EntityUpdated {

    @Override
    public int getUpdateTypeId() {
        return EntityUpdated.TYPE_CLOSE;
    }

    public static EntityUpdatedClose of() {
        return new EntityUpdatedClose();
    }

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.close();
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        return node;
    }

    public EntityUpdatedClose(BinaryStream stream) {
        read(stream);
    }

    private EntityUpdatedClose() {
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof EntityUpdatedClose;
    }

    @Override
    public void write(BinaryStream stream) {

    }

    @Override
    public void read(BinaryStream stream) {

    }

    @Override
    public String toString() {
        return "EntityUpdatedClose{}";
    }
}
