package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedTime implements LevelUpdated {

    private int time;

    private LevelUpdatedTime(int time) {
        this.time = time;
    }

    public LevelUpdatedTime(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedTime of(int time) {
        return new LevelUpdatedTime(time);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_TIME;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> level.setTime(this.time));
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putVarInt(this.time);
    }

    @Override
    public void read(BinaryStream stream) {
        this.time = stream.getVarInt();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedTime o)) return false;
        return this.time == o.time;
    }

    @Override
    public String toString() {
        return "LevelUpdatedTime{" +
            "time=" + time +
            '}';
    }

    @Override
    public int hashCode() {
        return this.time;
    }
}
