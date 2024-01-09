package net.easecation.ghosty.recording.level;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;

import java.util.LinkedList;
import java.util.List;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

public class LevelRecordImpl implements LevelRecord {

    private final List<RecordPair> rec = new LinkedList<>();

    public LevelRecordImpl(BinaryStream stream) {
        int len = (int) stream.getUnsignedVarInt();
        for (int i = 0; i < len; i++) {
            rec.add(new RecordPair(stream));
        }
    }

    public LevelRecordImpl() {}

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
        BinaryStream stream = new BinaryStream();
        stream.putByte(VERSION_0);
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

        public RecordPair(BinaryStream stream) {
            try {
                tick = (int) stream.getUnsignedVarInt();
                updated = LevelUpdated.fromBinaryStream(stream);
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
