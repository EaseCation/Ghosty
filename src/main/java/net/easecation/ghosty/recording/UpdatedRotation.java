package net.easecation.ghosty.recording;

import cn.nukkit.level.Location;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:26.
 */
class UpdatedRotation implements Updated {

    private double yaw;
    private double pitch;

    @Override
    public void processTo(PlaybackNPC ghost) {
        Location location = ghost.getLocation();
        location.yaw = yaw;
        location.pitch = pitch;
        ghost.teleport(location);
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setYaw(yaw);
        node.setPitch(pitch);
        return node;
    }

    static UpdatedRotation of(double yaw, double pitch) {
        return new UpdatedRotation(yaw, pitch);
    }

    @Override
    public int getUpdateTypeId() {
        return Updated.TYPE_ROTATION;
    }

    public UpdatedRotation(BinaryStream stream) {
        this.read(stream);
    }

    private UpdatedRotation(double yaw, double pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedRotation)) return false;
        UpdatedRotation o = (UpdatedRotation) obj;
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
}
