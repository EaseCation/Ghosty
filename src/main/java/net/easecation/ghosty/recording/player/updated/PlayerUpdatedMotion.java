package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

public class PlayerUpdatedMotion implements PlayerUpdated {

    private float x;
    private float y;
    private float z;

    public static PlayerUpdatedMotion of(float x, float y, float z) {
        return new PlayerUpdatedMotion(x, y, z);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_MOTION;
    }

    @Override
    public boolean hasStates() {
        return false;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        // 空
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        return node;
    }

    public PlayerUpdatedMotion(BinaryStream stream) {
        this.read(stream);
    }

    private PlayerUpdatedMotion(float x, float y, float z) {
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
        if(!(obj instanceof PlayerUpdatedMotion o)) return false;
        return (x == o.x) && (y == o.y) && (z==o.z);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putFloat(this.x);
        stream.putFloat(this.y);
        stream.putFloat(this.z);
    }

    @Override
    public void read(BinaryStream stream) {
        this.x = stream.getFloat();
        this.y = stream.getFloat();
        this.z = stream.getFloat();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedMotion{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            '}';
    }
}
