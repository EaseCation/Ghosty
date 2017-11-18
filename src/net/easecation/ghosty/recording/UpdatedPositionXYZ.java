package net.easecation.ghosty.recording;

import cn.nukkit.level.Location;
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

    private UpdatedPositionXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedPositionXYZ)) return false;
        UpdatedPositionXYZ o = (UpdatedPositionXYZ) obj;
        return (x == o.x) && (y == o.y) && (z==o.z);
    }
}
