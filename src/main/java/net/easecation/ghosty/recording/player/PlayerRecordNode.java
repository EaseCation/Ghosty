package net.easecation.ghosty.recording.player;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 记录了所有玩家信息的节点（tick）
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:13 17:02.
 * All rights reserved
 */
public final class PlayerRecordNode implements Serializable {
   /**
    * 序列化ID
    */
    private static final long serialVersionUID = -5809782578272943999L;

    private double x;
    private double y;
    private double z;

    private double yaw;
    private double pitch;
    private String level;

    private String tagName;
    private Item item;
    private Item armor0;
    private Item armor1;
    private Item armor2;
    private Item armor3;
    private Item offhand;

    private long dataFlags;
    private List<PlayerUpdated> extraUpdates = null;

    public static PlayerRecordNode of(Player player) {
        Item hand = null;
        if(player.getInventory() != null) hand = player.getInventory().getItemInHand();
        return new PlayerRecordNode(
                player.getX(),
                player.getY(),
                player.getZ(),
                player.getYaw(),
                player.getPitch(),
                player.getLevel().getName(),
                player.getDisplayName(),
                hand,
                player.getDataPropertyLong(Entity.DATA_FLAGS),
                player.getInventory().getArmorItem(0),
                player.getInventory().getArmorItem(1),
                player.getInventory().getArmorItem(2),
                player.getInventory().getArmorItem(3),
                player.getOffhandInventory().getItem()
        );
    }

    static PlayerRecordNode ZERO = new PlayerRecordNode(0,0,0,0,0,"","",null,0, null, null, null, null, null);

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PlayerRecordNode) {
            PlayerRecordNode node = (PlayerRecordNode) obj;
            return
                    this.x == node.x &&
                            this.y == node.y &&
                            this.z == node.z &&
                            this.yaw == node.yaw &&
                            this.pitch == node.pitch &&
                            this.level.equals(node.level) &&
                            this.tagName.equals(node.tagName) &&
                            this.item.equals(node.item) &&
                            this.armor0.equals(node.armor0) &&
                            this.armor1.equals(node.armor1) &&
                            this.armor2.equals(node.armor2) &&
                            this.armor3.equals(node.armor3) &&
                            this.offhand.equals(node.offhand) &&
                            this.dataFlags == node.dataFlags
                ;
        }
        return false;
    }

    private PlayerRecordNode(double x, double y, double z, double yaw, double pitch, String level, String tagName, Item item, long dataFlags, Item armor0, Item armor1, Item armor2, Item armor3, Item offhand) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.level = level;
        this.tagName = tagName;
        this.item = item;
        this.dataFlags = dataFlags;
        this.armor0 = armor0;
        this.armor1 = armor1;
        this.armor2 = armor2;
        this.armor3 = armor3;
        this.offhand = offhand;
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    public double getZ() {
        return z;
    }

    public void setZ(double z) {
        this.z = z;
    }

    public double getYaw() {
        return yaw;
    }

    public void setYaw(double yaw) {
        this.yaw = yaw;
    }

    public double getPitch() {
        return pitch;
    }

    public void setPitch(double pitch) {
        this.pitch = pitch;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public Item getArmor0() {
        return armor0;
    }

    public void setArmor0(Item armor0) {
        this.armor0 = armor0;
    }

    public Item getArmor1() {
        return armor1;
    }

    public void setArmor1(Item armor1) {
        this.armor1 = armor1;
    }

    public Item getArmor2() {
        return armor2;
    }

    public void setArmor2(Item armor2) {
        this.armor2 = armor2;
    }

    public Item getArmor3() {
        return armor3;
    }

    public void setArmor3(Item armor3) {
        this.armor3 = armor3;
    }

    public Item getOffhand() {
        return offhand;
    }

    public void setOffhand(Item offhand) {
        this.offhand = offhand;
    }

    public long getDataFlags() {
        return dataFlags;
    }

    public void setDataFlags(long dataFlags) {
        this.dataFlags = dataFlags;
    }

    public List<PlayerUpdated> getExtraUpdates() {
        if (this.extraUpdates == null) {
            this.extraUpdates = new ArrayList<>();
        }
        return extraUpdates;
    }

    public void resetExtraUpdates() {
        if (this.extraUpdates == null) {
            this.extraUpdates = new ArrayList<>();
        } else {
            this.extraUpdates.clear();
        }
    }

    public void offerExtraUpdate(PlayerUpdated update) {
        if (this.extraUpdates == null) {
            this.extraUpdates = new ArrayList<>();
        }
        this.extraUpdates.add(update);
    }

    public void offerExtraUpdate(List<PlayerUpdated> updates) {
        if (this.extraUpdates == null) {
            this.extraUpdates = new ArrayList<>();
        }
        this.extraUpdates.addAll(updates);
    }
}
