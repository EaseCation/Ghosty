package net.easecation.ghosty.recording;

import cn.nukkit.item.Item;

public class PlayerRecordTick {

    public double x;
    public double y;
    public double z;

    public double yaw;
    public double pitch;
    public String level;

    public String tagName;
    public Item item;

    public PlayerRecordTick(double x, double y, double z, double yaw, double pitch, String level, String tagName, Item item) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.level = level;
        this.tagName = tagName;
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerRecordTick) {
            PlayerRecordTick tick = (PlayerRecordTick) obj;
            return
                    this.x == tick.x &&
                    this.y == tick.y &&
                    this.z == tick.z &&
                    this.yaw == tick.yaw &&
                    this.pitch == tick.pitch &&
                    this.level.equals(tick.level) &&
                    this.tagName.equals(tick.tagName) &&
                    this.item.equals(tick.item);
        }
        return false;
    }
}
