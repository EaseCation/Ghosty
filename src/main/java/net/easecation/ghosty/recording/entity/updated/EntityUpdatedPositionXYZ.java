package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:22.
 */
public class EntityUpdatedPositionXYZ implements EntityUpdated {
    private double x;
    private double y;
    private double z;

    public static EntityUpdatedPositionXYZ of(double x, double y, double z) {
        return new EntityUpdatedPositionXYZ(x, y, z);
    }

    @Override
    public int getUpdateTypeId() {
        return EntityUpdated.TYPE_POSITION_XYZ;
    }

    @Override
    public void processTo(SimulatedEntity entity) {
        Location location = entity.getLocation();
        location.x = x;
        location.y = y;
        location.z = z;
        entity.teleport(location);
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setX(x);
        node.setY(y);
        node.setZ(z);
        return node;
    }

    public EntityUpdatedPositionXYZ(BinaryStream stream) {
        this.read(stream);
    }

    private EntityUpdatedPositionXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 asVector3() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedPositionXYZ o)) return false;
        return (x == o.x) && (y == o.y) && (z==o.z);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putFloat((float) this.x);
        stream.putFloat((float) this.y);
        stream.putFloat((float) this.z);
    }

    @Override
    public void read(BinaryStream stream) {
        this.x = stream.getFloat();
        this.y = stream.getFloat();
        this.z = stream.getFloat();
    }

    @Override
    public String toString() {
        return "EntityUpdatedPositionXYZ{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }
}
