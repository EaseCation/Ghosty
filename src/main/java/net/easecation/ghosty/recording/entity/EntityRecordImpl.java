package net.easecation.ghosty.recording.entity;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.updated.*;
import net.easecation.ghosty.util.LittleEndianBinaryStream;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
public class EntityRecordImpl implements EntityRecord {

    private EntityRecordNode last = EntityRecordNode.ZERO;

    private final List<RecordPair> rec = new LinkedList<>();

    private final long entityId;
    private final int networkId;
    private final String entityIdentifier;

    public EntityRecordImpl(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 1: {
                stream = new LittleEndianBinaryStream(stream);
                this.entityId = stream.getEntityUniqueId();
                this.networkId = stream.getVarInt();
                this.entityIdentifier = stream.getString();
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                break;
            }
            case 0: {
                this.entityId = stream.getEntityUniqueId();
                this.networkId = stream.getVarInt();
                this.entityIdentifier = stream.getString();
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported format version: " + formatVersion);
        }
    }

    public EntityRecordImpl(Entity entity) {
        this.entityId = entity.getId();
        this.networkId = entity.getNetworkId();
        this.entityIdentifier = entity.getNetworkId() > 0 ? "" : entity.getIdentifier();
    }

    @Override
    public long getEntityId() {
        return entityId;
    }

    @Override
    public int getNetworkId() {
        return networkId;
    }

    @Override
    public String getEntityIdentifier() {
        return entityIdentifier;
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new LittleEndianBinaryStream();
        stream.putByte(EntityRecord.OBJECT_V1);
        stream.putEntityUniqueId(this.entityId);
        stream.putVarInt(this.networkId);
        stream.putString(this.entityIdentifier);
        stream.putUnsignedVarInt(this.rec.size());
        for (RecordPair pair : this.rec) {
            pair.write(stream);
        }
        return stream.getBuffer();
    }

    @Override
    public void record(int tick, EntityRecordNode node) {
        double lx = last.getX(), x = node.getX();
        double ly = last.getY(), y = node.getY();
        double lz = last.getZ(), z = node.getZ();
        if (lx != x || ly != y || lz != z) {
            push(tick, EntityUpdatedPositionXYZ.of(x, y, z));
        }
        double la = last.getYaw(), a = node.getYaw();
        double lp = last.getPitch(), p = node.getPitch();
        if (la != a || lp != p) {
            push(tick, EntityUpdatedRotation.of(a, p));
        }
        String ln = last.getTagName(), n = node.getTagName();
        if (!Objects.equals(ln, n)) {
            push(tick, EntityUpdatedTagName.of(n));
        }
        String ls = last.getScoreTag(), s = node.getScoreTag();
        if (!Objects.equals(ls, s)) {
            push(tick, EntityUpdatedScoreTag.of(s));
        }
        Item li = last.getItem(), i = node.getItem();
        if (!Objects.equals(li, i)) {
            push(tick, EntityUpdatedItem.of(i));
        }
        if (li != null && i != null && li.getDamage() != i.getDamage()) {
            push(tick, EntityUpdatedItem.of(i));
        }
        long lastFlags = last.getDataFlags(), flags = node.getDataFlags();
        if (lastFlags != flags) {
            push(tick, EntityUpdatedDataFlags.of(flags));
        }
        float lastScale = last.getScale(), scale = node.getScale();
        if (lastScale != scale) {
            push(tick, EntityUpdatedScale.of(scale));
        }
        boolean lastNameTagAlwaysVisible = last.isNameTagAlwaysVisible(), nameTagAlwaysVisible = node.isNameTagAlwaysVisible();
        if (lastNameTagAlwaysVisible != nameTagAlwaysVisible) {
            push(tick, EntityUpdatedNameTagAlwaysVisible.of(nameTagAlwaysVisible));
        }
        int lastSkinId = last.getSkinId(), skinId = node.getSkinId();
        if (lastSkinId != skinId) {
            push(tick, EntityUpdatedSkinId.of(skinId));
        }
        int lastNpcSkinId = last.getNpcSkinId(), npcSkinId = node.getNpcSkinId();
        if (lastNpcSkinId != npcSkinId) {
            push(tick, EntityUpdatedNPCSkinId.of(npcSkinId));
        }
        int lastVariant = last.getVariant(), variant = node.getVariant();
        if (lastVariant != variant) {
            push(tick, EntityUpdatedVariant.of(variant));
        }
        int lastMarkVariant = last.getMarkVariant(), markVariant = node.getMarkVariant();
        if (lastMarkVariant != markVariant) {
            push(tick, EntityUpdatedMarkVariant.of(markVariant));
        }
        SimulatedEntity.SkinInfo lastSkinInfo = last.getSkinInfo(), skinInfo = node.getSkinInfo();
        if (!Objects.equals(lastSkinInfo, skinInfo)) {
            push(tick, EntityUpdatedSkinInfo.of(skinInfo.geoName(), skinInfo.dataHash()));
        }
        last = node;
    }

    @Override
    public void recordClose(int tick) {
        push(tick, EntityUpdatedClose.of());
    }

    private void push(int tick, EntityUpdated updated) {
        rec.add(new RecordPair(tick, updated));
        if (DEBUG_DUMP) {
            if (updated.getUpdateTypeId() != EntityUpdated.TYPE_POSITION_XYZ && updated.getUpdateTypeId() != EntityUpdated.TYPE_ROTATION) {
                GhostyPlugin.getInstance().getLogger().debug("entity[" + this.entityId + "] " + tick + " -> " + updated);
            }
        }
    }

    /**
     * tick 与 PlayerUpdated 对的封装类
     */
    private static class RecordPair {

        private RecordPair(BinaryStream stream, int formatVersion) {
            try {
                this.tick = (int) stream.getUnsignedVarInt();
                this.updated = EntityUpdated.fromBinaryStream(stream, formatVersion);
            } catch (Exception e) {
                Server.getInstance().getLogger().logException(e);
                throw e;
            }
        }

        private RecordPair(int tick, EntityUpdated updated) {
            this.tick = tick;
            this.updated = updated;
        }

        int tick; EntityUpdated updated;

        private void write(BinaryStream stream) {
            stream.putUnsignedVarInt(tick);
            stream.putByte((byte) updated.getUpdateTypeId());
            updated.write(stream);
        }
    }

    @Override
    public PlaybackIterator<EntityUpdated> iterator() {
        PlaybackIterator<EntityUpdated> recordIterator = new PlaybackIterator<>();
        rec.forEach(e -> recordIterator.insert(e.tick, e.updated));
        return recordIterator;
    }

}
