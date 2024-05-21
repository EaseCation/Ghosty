package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedOverload implements LevelUpdated {

    private int tickUseMillis;

    private LevelUpdatedOverload(int tickUseMillis) {
        this.tickUseMillis = tickUseMillis;
    }

    public LevelUpdatedOverload(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedOverload of(int tickUseMillis) {
        return new LevelUpdatedOverload(tickUseMillis);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_OVERLOAD;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        // 不做事情
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putUnsignedVarInt(this.tickUseMillis);
    }

    @Override
    public void read(BinaryStream stream) {
        this.tickUseMillis = (int) stream.getUnsignedVarInt();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedOverload o)) return false;
        return this.tickUseMillis == o.tickUseMillis;
    }

    @Override
    public String toString() {
        return "LevelUpdatedOverload{" +
            "time=" + tickUseMillis +
            '}';
    }

    @Override
    public int hashCode() {
        return this.tickUseMillis;
    }
}
