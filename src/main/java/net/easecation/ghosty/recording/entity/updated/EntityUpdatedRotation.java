package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.level.Location;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:26.
 */
public class EntityUpdatedRotation implements EntityUpdated {

    private double yaw;
    private double pitch;

    @Override
    public void processTo(SimulatedEntity entity) {
        Location location = entity.getLocation();
        location.yaw = yaw;
        location.pitch = pitch;
        entity.teleport(location);
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setYaw(yaw);
        node.setPitch(pitch);
        return node;
    }

    public static EntityUpdatedRotation of(double yaw, double pitch) {
        return new EntityUpdatedRotation(yaw, pitch);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_ROTATION;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public EntityUpdatedRotation(BinaryStream stream) {
        this.read(stream);
    }

    private EntityUpdatedRotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedRotation o)) return false;
        return (yaw == o.yaw) && (pitch == o.pitch);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putFloat((float) this.yaw);
        stream.putFloat((float) this.pitch);
    }

    @Override
    public void read(BinaryStream stream) {
        this.yaw = stream.getFloat();
        this.pitch = stream.getFloat();
    }

    @Override
    public String toString() {
        return "EntityUpdatedRotation{" +
            "yaw=" + yaw +
            ", pitch=" + pitch +
            '}';
    }
}
