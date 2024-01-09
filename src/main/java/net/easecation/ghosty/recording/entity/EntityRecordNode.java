package net.easecation.ghosty.recording.entity;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.EntityID;
import cn.nukkit.entity.item.EntityItem;
import cn.nukkit.item.Item;
import cn.nukkit.nbt.NBTIO;
import net.easecation.ghosty.entity.SimulatedEntity;

import java.io.Serializable;
import java.util.Objects;

public final class EntityRecordNode implements Serializable {

    private double x;
    private double y;
    private double z;

    private double yaw;
    private double pitch;

    private String tagName;
    private String scoreTag;
    private Item item;

    private long dataFlags;
    private float scale;
    private boolean nameTagAlwaysVisible;
    private int skinId;
    private int npcSkinId;
    private int varint;
    private int markVariant;

    private SimulatedEntity.SkinInfo skinInfo;

    public static EntityRecordNode of(Entity entity) {
        Item item = null;
        SimulatedEntity.SkinInfo skinInfo = null;
        if (entity instanceof EntityItem entityItem) {
            item = entityItem.getItem();
        } else if (entity instanceof EntityHuman human) {
            item = human.getInventory().getItemInHand();
            if (human.getSkin() != null) {
                skinInfo = SimulatedEntity.SkinInfo.fromSkin(human.getSkin());
            }
        } else if (entity.getNetworkId() == EntityID.ITEM) {
            // 尝试从nbt读取物品
            if (entity.namedTag != null && entity.namedTag.contains("Item")) {
                item = NBTIO.getItemHelper(entity.namedTag.getCompound("Item"));
            }
        }
        return new EntityRecordNode(
            entity.getX(),
            entity.getY(),
            entity.getZ(),
            entity.getYaw(),
            entity.getPitch(),
            entity.getNameTag(),
            entity.getScoreTag(),
            item,
            entity.getDataPropertyLong(Entity.DATA_FLAGS),
            entity.getScale(),
            entity.isNameTagAlwaysVisible(),
            entity.getDataPropertyInt(Entity.DATA_SKIN_ID),
            entity.getDataPropertyInt(Entity.DATA_NPC_SKIN_ID),
            entity.getDataPropertyInt(Entity.DATA_VARIANT),
            entity.getDataPropertyInt(Entity.DATA_MARK_VARIANT),
            skinInfo
        );
    }

    static EntityRecordNode ZERO = createZero();

    public static EntityRecordNode createZero() {
        return new EntityRecordNode(0,0,0,0, 0,"","",null,0,1,false,0, 0, 0,0, null);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof EntityRecordNode node) {
            return this.x == node.x &&
                    this.y == node.y &&
                    this.z == node.z &&
                    this.yaw == node.yaw &&
                    this.pitch == node.pitch &&
                    this.tagName.equals(node.tagName) &&
                    this.scoreTag.equals(node.scoreTag) &&
                    this.item.equals(node.item) &&
                    this.dataFlags == node.dataFlags &&
                    this.scale == node.scale &&
                    this.nameTagAlwaysVisible == node.nameTagAlwaysVisible
                    && this.skinId == node.skinId
                    && this.npcSkinId == node.npcSkinId
                    && this.varint == node.varint
                    && this.markVariant == node.markVariant
                    && Objects.equals(this.skinInfo, node.skinInfo);
        }
        return false;
    }

    private EntityRecordNode(double x, double y, double z, double yaw, double pitch,
                             String tagName, String scoreTag, Item item,
                             long dataFlags, float scale, boolean nameTagAlwaysVisible,
                             int skinId, int npcSkinId, int varint, int markVariant, SimulatedEntity.SkinInfo skinInfo) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.tagName = tagName;
        this.scoreTag = scoreTag;
        this.item = item;
        this.dataFlags = dataFlags;
        this.scale = scale;
        this.nameTagAlwaysVisible = nameTagAlwaysVisible;
        this.skinId = skinId;
        this.npcSkinId = npcSkinId;
        this.varint = varint;
        this.markVariant = markVariant;
        this.skinInfo = skinInfo;
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

    public String getTagName() {
        return tagName;
    }

    public void setTagName(String tagName) {
        this.tagName = tagName;
    }

    public String getScoreTag() {
        return scoreTag;
    }

    public void setScoreTag(String scoreTag) {
        this.scoreTag = scoreTag;
    }

    public Item getItem() {
        return item;
    }

    public void setItem(Item item) {
        this.item = item;
    }

    public long getDataFlags() {
        return dataFlags;
    }

    public void setDataFlags(long dataFlags) {
        this.dataFlags = dataFlags;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isNameTagAlwaysVisible() {
        return nameTagAlwaysVisible;
    }

    public void setNameTagAlwaysVisible(boolean nameTagAlwaysVisible) {
        this.nameTagAlwaysVisible = nameTagAlwaysVisible;
    }

    public int getSkinId() {
        return skinId;
    }

    public void setSkinId(int skinId) {
        this.skinId = skinId;
    }

    public int getNpcSkinId() {
        return npcSkinId;
    }

    public void setNpcSkinId(int npcSkinId) {
        this.npcSkinId = npcSkinId;
    }

    public int getVariant() {
        return varint;
    }

    public void setVarint(int varint) {
        this.varint = varint;
    }

    public int getMarkVariant() {
        return markVariant;
    }

    public void setMarkVariant(int markVariant) {
        this.markVariant = markVariant;
    }

    public SimulatedEntity.SkinInfo getSkinInfo() {
        return skinInfo;
    }

    public void setSkinInfo(SimulatedEntity.SkinInfo skinInfo) {
        this.skinInfo = skinInfo;
    }
}
