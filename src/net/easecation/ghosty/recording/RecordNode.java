package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.item.Item;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:13 17:02.
 * All rights reserved
 */
public final class RecordNode {
    private double x;
    private double y;
    private double z;

    private double yaw;
    private double pitch;
    private String level;

    private String tagName;
    private Item item;

    static RecordNode of(Player player) {
        Item hand = null;
        if(player.getInventory() != null) hand = player.getInventory().getItemInHand();
        return new RecordNode(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.getLevel().getName(),
                player.getDisplayName(),
                hand
        );
    }

    static RecordNode ZERO = new RecordNode(0,0,0,0,0,"","",null);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RecordNode) {
            RecordNode node = (RecordNode) obj;
            return
                    this.x == node.x &&
                            this.y == node.y &&
                            this.z == node.z &&
                            this.yaw == node.yaw &&
                            this.pitch == node.pitch &&
                            this.level.equals(node.level) &&
                            this.tagName.equals(node.tagName) &&
                            this.item.equals(node.item);
        }
        return false;
    }

    private RecordNode(double x, double y, double z, double yaw, double pitch, String level, String tagName, Item item) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.level = level;
        this.tagName = tagName;
        this.item = item;
    }

    double getX() {
        return x;
    }

    void setX(double x) {
        this.x = x;
    }

    double getY() {
        return y;
    }

    void setY(double y) {
        this.y = y;
    }

    double getZ() {
        return z;
    }

    void setZ(double z) {
        this.z = z;
    }

    double getYaw() {
        return yaw;
    }

    void setYaw(double yaw) {
        this.yaw = yaw;
    }

    double getPitch() {
        return pitch;
    }

    void setPitch(double pitch) {
        this.pitch = pitch;
    }

    String getLevel() {
        return level;
    }

    void setLevel(String level) {
        this.level = level;
    }

    String getTagName() {
        return tagName;
    }

    void setTagName(String tagName) {
        this.tagName = tagName;
    }

    Item getItem() {
        return item;
    }

    void setItem(Item item) {
        this.item = item;
    }
}
