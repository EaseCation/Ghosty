package net.easecation.ghosty.recording.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.MathUtil;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.player.updated.*;
import net.easecation.ghosty.util.LittleEndianBinaryStream;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
public class SkinlessPlayerRecord implements PlayerRecord {

    private PlayerRecordNode last = PlayerRecordNode.ZERO;

    private final List<RecordPair> rec = new LinkedList<>();

    private final String playerName;

    private Skin tempSkin = null;

    public SkinlessPlayerRecord(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 1: {
                stream = new LittleEndianBinaryStream(stream);
                this.playerName = stream.getString();
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                break;
            }
            case 0: {
                this.playerName = stream.getString();
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

    public SkinlessPlayerRecord(Player player) {
        this.playerName = player.getName();
    }

    @Override
    public String getPlayerName() {
        return playerName;
    }

    @Override
    public void record(int tick, PlayerRecordNode node) {
        double lx = last.getX(), x = node.getX();
        double ly = last.getY(), y = node.getY();
        double lz = last.getZ(), z = node.getZ();
        if (lx != x || ly != y || lz != z) {
            push(tick, PlayerUpdatedPositionXYZ.of(x, y, z));
        }
        double la = last.getYaw(), a = node.getYaw();
        double lp = last.getPitch(), p = node.getPitch();
        if (la != a || lp != p) {
            push(tick, PlayerUpdatedRotation.of(a, p));
        }
        String ln = last.getTagName(), n = node.getTagName();
        if (!Objects.equals(ln, n)) {
            push(tick, PlayerUpdatedTagName.of(n));
        }
        String lw = last.getLevel(), w = node.getLevel();
        if (!Objects.equals(lw, w)) {
            push(tick, PlayerUpdatedWorldChanged.of(w));
        }
        Item li = last.getItem(), i = node.getItem();
        if (li == null || !li.equals(i) || li.getCount() != i.getCount()) {
            push(tick, PlayerUpdatedItem.of(i));
        }
        long lastFlags = last.getDataFlags(), flags = node.getDataFlags();
        if (lastFlags != flags) {
            push(tick, PlayerUpdatedDataFlags.of(flags));
        }
        Item la0 = last.getArmor0(), a0 = node.getArmor0();
        if (!Objects.equals(la0, a0)) {
            push(tick, PlayerUpdatedArmor0.of(a0));
        }
        Item la1 = last.getArmor1(), a1 = node.getArmor1();
        if (!Objects.equals(la1, a1)) {
            push(tick, PlayerUpdatedArmor1.of(a1));
        }
        Item la2 = last.getArmor2(), a2 = node.getArmor2();
        if (!Objects.equals(la2, a2)) {
            push(tick, PlayerUpdatedArmor2.of(a2));
        }
        Item la3 = last.getArmor3(), a3 = node.getArmor3();
        if (!Objects.equals(la3, a3)) {
            push(tick, PlayerUpdatedArmor3.of(a3));
        }
        Item lo = last.getOffhand(), o = node.getOffhand();
        if (!Objects.equals(lo, o)) {
            push(tick, PlayerUpdatedOffhand.of(o));
        }

        for (PlayerUpdated extraUpdate : node.getExtraUpdates()) {
            push(tick, extraUpdate);
        }
        node.resetExtraUpdates();
        last = node;
    }

    private void push(int tick, PlayerUpdated updated) {
        rec.add(new RecordPair(tick, updated));
        if (DEBUG_DUMP) {
            if (updated.getUpdateTypeId() != PlayerUpdated.TYPE_POSITION_XYZ && updated.getUpdateTypeId() != PlayerUpdated.TYPE_ROTATION) {
                GhostyPlugin.getInstance().getLogger().debug(tick + " -> " + updated);
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
                this.updated = PlayerUpdated.fromBinaryStream(stream, formatVersion);
            } catch (Exception e) {
                Server.getInstance().getLogger().logException(e);
                throw e;
            }
        }

        private RecordPair(int tick, PlayerUpdated updated) {
            this.tick = tick;
            this.updated = updated;
        }

        int tick; PlayerUpdated updated;

        private void write(BinaryStream stream) {
            stream.putUnsignedVarInt(tick);
            stream.putByte((byte) updated.getUpdateTypeId());
            updated.write(stream);
        }
    }

    @Override
    public PlaybackIterator<PlayerUpdated> iterator() {
        PlaybackIterator<PlayerUpdated> recordIterator = new PlaybackIterator<>();
        rec.forEach(e -> recordIterator.insert(e.tick, e.updated));
        return recordIterator;
    }

    /**
     * 允许在外部临时设置皮肤（异步从别处设置皮肤）
     * @param skin Skin
     */
    public void setSkin(Skin skin) {
        this.tempSkin = skin;
    }

    @Override
    public Skin getSkin() {
        return this.tempSkin;
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new LittleEndianBinaryStream();
        stream.putByte(PlayerRecord.OBJECT_SKINLESS_V1);
        stream.putString(this.playerName);
        stream.putUnsignedVarInt(this.rec.size());
        for (RecordPair pair : this.rec) {
            pair.write(stream);
        }
        return stream.getBuffer();
    }

    public double getMaxMovement() {
        Vector3 lastPos = null;
        double maxMovement = 0;
        for (RecordPair pair : this.rec.stream().filter(p -> p.updated instanceof PlayerUpdatedPositionXYZ).toList()) {
            PlayerUpdatedPositionXYZ pos = (PlayerUpdatedPositionXYZ) pair.updated;
            Vector3 newPos = pos.asVector3();
            if (lastPos != null) {
                double distance = newPos.distance(lastPos);
                if (distance > maxMovement) maxMovement = distance;
            }
            lastPos = newPos;
        }
        return maxMovement;
    }

    public double calculateMovementVariance() {
        Vector3 lastPos = null;

        List<RecordPair> pairs = this.rec.stream().filter(p -> p.updated instanceof PlayerUpdatedPositionXYZ).toList();
        if (pairs.size() <= 1) return 0;
        double[] distances = new double[pairs.size() - 1];
        for (int i = 0; i < pairs.size(); i++) {
            RecordPair pair = pairs.get(i);
            PlayerUpdatedPositionXYZ pos = (PlayerUpdatedPositionXYZ) pair.updated;
            Vector3 newPos = pos.asVector3();
            if (lastPos != null) {
                distances[i - 1] = newPos.distance(lastPos);
            }
            lastPos = newPos;
        }
        return MathUtil.getVariance(distances);
    }
}
