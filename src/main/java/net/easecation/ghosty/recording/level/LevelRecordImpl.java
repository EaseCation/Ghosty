package net.easecation.ghosty.recording.level;

import cn.nukkit.GameVersion;
import cn.nukkit.Server;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;
import net.easecation.ghosty.recording.level.updated.LevelUpdatedOverload;
import net.easecation.ghosty.util.LittleEndianBinaryStream;

import java.util.LinkedList;
import java.util.List;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

public class LevelRecordImpl implements LevelRecord {

    private final List<RecordPair> rec = new LinkedList<>();

    private final int baseGameVersionProtocol;

    public LevelRecordImpl(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 2: {
                stream = new LittleEndianBinaryStream(stream);
                baseGameVersionProtocol = stream.getInt();
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    rec.add(new RecordPair(stream, formatVersion));
                }
                break;
            }
            case 1: {
                stream = new LittleEndianBinaryStream(stream);
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    rec.add(new RecordPair(stream, formatVersion));
                }
                baseGameVersionProtocol = 0;
                break;
            }
            case 0: {
                int len = (int) stream.getUnsignedVarInt();
                for (int i = 0; i < len; i++) {
                    rec.add(new RecordPair(stream, formatVersion));
                }
                baseGameVersionProtocol = 0;
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported format version: " + formatVersion);
        }
    }

    public LevelRecordImpl() {
        baseGameVersionProtocol = GameVersion.getFeatureVersion().getProtocol();
    }

    @Override
    public int getBaseGameVersionProtocol() {
        return baseGameVersionProtocol;
    }

    /**
     * 每Tick调用，储存LevelRecordNode中的更新到Updated录制列表中
     * @param tick 当前Tick
     * @param node LevelRecordNode
     */
    @Override
    public void record(int tick, LevelRecordNode node) {
        for (LevelUpdated updated : node.toUpdated()) {
            push(tick, updated);
        }
        // 服务器过载时（tick用时超过50ms），录制overload
        long tickUseNano = Server.getInstance().getTickUseNano();
        if (tickUseNano > 50_000_000) {
            push(tick, LevelUpdatedOverload.of((int) (tickUseNano / 1_000_000)));
        }
        node.clear();
    }

    private void push(int tick, LevelUpdated updated) {
        rec.add(new RecordPair(tick, updated));
        if (DEBUG_DUMP) {
            if (updated.getUpdateTypeId() != LevelUpdated.TYPE_LEVEL_EVENT) {
                GhostyPlugin.getInstance().getLogger().debug(tick + " -> " + updated);
            }
        }
    }

    @Override
    public List<LevelUpdated> getRecDataUnsafe() {
        return this.rec.stream().map(e -> e.updated).toList();
    }

    @Override
    public PlaybackIterator<LevelUpdated> iterator() {
        PlaybackIterator<LevelUpdated> iterator = new PlaybackIterator<>();
        rec.forEach((e) -> {
            // GhostyPlugin.getInstance().getLogger().debug("queue: " + e.tick + " -> " + e.updated);
            iterator.insert(e.tick, e.updated);
        });
        return iterator;
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new LittleEndianBinaryStream();
        stream.putByte(VERSION_2);
        stream.putInt(baseGameVersionProtocol);
        stream.putUnsignedVarInt(this.rec.size());
        for (RecordPair pair : this.rec) {
            pair.write(stream);
        }
        return stream.getBuffer();
    }

    public static class RecordPair {

        int tick; LevelUpdated updated;

        private RecordPair(int tick, LevelUpdated updated) {
            this.tick = tick;
            this.updated = updated;
        }

        public RecordPair(BinaryStream stream, int formatVersion) {
            try {
                tick = (int) stream.getUnsignedVarInt();
                updated = LevelUpdated.fromBinaryStream(stream, formatVersion);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public void write(BinaryStream stream) {
            stream.putUnsignedVarInt(tick);
            stream.putByte((byte) updated.getUpdateTypeId());
            updated.write(stream);
        }
    }

}
