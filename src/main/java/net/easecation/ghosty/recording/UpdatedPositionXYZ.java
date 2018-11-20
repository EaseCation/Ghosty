package net.easecation.ghosty.recording;

import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:22.
 */
class UpdatedPositionXYZ implements Updated {
    private double x;
    private double y;
    private double z;

    static UpdatedPositionXYZ of(double x, double y, double z) {
        return new UpdatedPositionXYZ(x, y, z);
    }

    @Override
    public int getUpdateTypeId() {
        return Updated.TYPE_POSITION_XYZ;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        Location location = ghost.getLocation();
        location.x = x;
        location.y = y;
        location.z = z;
        ghost.teleport(location);
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setX(x);
        node.setY(y);
        node.setZ(z);
        return node;
    }

    public UpdatedPositionXYZ(BinaryStream stream) {
        this.read(stream);
    }

    private UpdatedPositionXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vector3 asVector3() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedPositionXYZ)) return false;
        UpdatedPositionXYZ o = (UpdatedPositionXYZ) obj;
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
}
