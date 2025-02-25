package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Player;
import cn.nukkit.level.Location;
import cn.nukkit.math.Mth;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.TextFormat;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.playback.PlayerPlaybackEngine;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:22.
 */
public class PlayerUpdatedPositionXYZ implements PlayerUpdated {
    private double x;
    private double y;
    private double z;

    public static PlayerUpdatedPositionXYZ of(double x, double y, double z) {
        return new PlayerUpdatedPositionXYZ(x, y, z);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_POSITION_XYZ;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        Location location = ghost.getLocation();
        location.x = x;
        location.y = y;
        location.z = z;
        ghost.teleport(location);
        PlayerPlaybackEngine engine = ghost.getEngine();
        if (engine == null) {
            return;
        }
        if (ghost.lastPosition == null) {
            ghost.lastPosition = location;
            ghost.lastMoveTick = engine.getTick();
        } else {
            if (engine.displayMovingSpeed && engine.getTick() % 10 == 0) {
                // 计算距离
                double distance = location.distance(ghost.lastPosition) / (engine.getTick() - ghost.lastMoveTick) * 20;
                if (distance >= 0.01) {
                    String distanceStr = String.valueOf(Mth.round(distance, 3));
                    // 显示速度（包含双方的Ping数据）
                    String msg = "[Moving] " + distanceStr + TextFormat.WHITE + " "
                        + "[" + PlayerUpdatedPing.getDisplayPing(ghost.lastPing) + TextFormat.WHITE + "]" + ghost.getAliasName();
                    for (Player viewer : ghost.getViewers().values()) {
                        viewer.sendMessage(msg);
                    }
                }
            }

            ghost.lastPosition = location;
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setX(x);
        node.setY(y);
        node.setZ(z);
        return node;
    }

    public PlayerUpdatedPositionXYZ(BinaryStream stream) {
        this.read(stream);
    }

    private PlayerUpdatedPositionXYZ(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public Vector3 asVector3() {
        return new Vector3(this.x, this.y, this.z);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedPositionXYZ o)) return false;
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
        return "PlayerUpdatedPositionXYZ{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }
}
