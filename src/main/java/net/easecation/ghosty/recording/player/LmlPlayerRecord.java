package net.easecation.ghosty.recording.player;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.MathUtil;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.player.updated.*;
import net.easecation.ghosty.util.LittleEndianBinaryStream;
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
public class LmlPlayerRecord implements PlayerRecord {

    private PlayerRecordNode last = PlayerRecordNode.ZERO;

    private final List<RecordPair> rec = new LinkedList<>();

    private final int protocol;
    private final String playerName;
    private Skin skin;

    public LmlPlayerRecord(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 2: {
                stream = new LittleEndianBinaryStream(stream);
                this.protocol = stream.getInt();
                this.playerName = stream.getString();
                this.skin = PersistenceBinaryStreamHelper.getSkin(stream);
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                break;
            }
            case 1: {
                stream = new LittleEndianBinaryStream(stream);
                this.playerName = stream.getString();
                this.skin = PersistenceBinaryStreamHelper.getSkin(stream);
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                this.protocol = 0;
                break;
            }
            case 0: {
                this.playerName = stream.getString();
                int offset = stream.getOffset();
                try {
                    this.skin = stream.getSkinLegacy();
                } catch (IllegalArgumentException e) {
                    stream.setOffset(offset);
                    this.skin = stream.getSkin();
                }
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    RecordPair pair = new RecordPair(stream, formatVersion);
                    rec.add(pair);
                }
                this.protocol = 0;
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported format version: " + formatVersion);
        }
    }

    public LmlPlayerRecord(Player player) {
        this.protocol = player.getProtocol();
        this.playerName = player.getName();
        this.skin = player.getSkin();
    }

    @Override
    public int getProtocol() {
        return protocol;
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
        if (lx != x || ly != y || lz != z)
            push(tick, PlayerUpdatedPositionXYZ.of(x, y, z));
        double la = last.getYaw(), a = node.getYaw();
        double lp = last.getPitch(), p = node.getPitch();
        if(la != a || lp != p)
            push(tick, PlayerUpdatedRotation.of(a, p));
        String ln = last.getTagName(), n = node.getTagName();
        if(!Objects.equals(ln, n))
            push(tick, PlayerUpdatedTagName.of(n));
        String lw = last.getLevel(), w = node.getLevel();
        if(!Objects.equals(lw, w))
            push(tick, PlayerUpdatedWorldChanged.of(w));
        Item li = last.getItem(), i = node.getItem();
        if(!Objects.equals(li, i))
            push(tick, PlayerUpdatedItem.of(i));
        long lastFlags = last.getDataFlags(), flags = node.getDataFlags();
        if(lastFlags != flags)
            push(tick, PlayerUpdatedDataFlags.of(flags));
        last = node;
    }

    private void push(int tick, PlayerUpdated updated) {
        rec.add(new RecordPair(tick, updated));
    }

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
        PlaybackIterator<PlayerUpdated> iterator = new PlaybackIterator<>();
        rec.forEach((e) -> iterator.insert(e.tick, e.updated));
        return iterator;
    }

    @Override
    public Skin getSkin() {
        return skin;
    }

    @Override
    public long getOriginEntityId() {
        return 0;
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new LittleEndianBinaryStream();
        stream.putByte(PlayerRecord.OBJECT_LML_V2);
        stream.putInt(this.protocol);
        stream.putString(this.playerName);
        PersistenceBinaryStreamHelper.putSkin(stream, this.skin);
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

    @Override
    public List<PlayerUpdated> getRecDataUnsafe() {
        return this.rec.stream().map(p -> p.updated).toList();
    }

}
