package net.easecation.ghosty.recording;

import cn.nukkit.IPlayer;
import cn.nukkit.entity.data.Skin;

import java.util.HashMap;
import java.util.Map;

public class PlayerRecord {

    private IPlayer player;
    private Skin skin;

    private Map<Integer, PlayerRecordTick> ticks = new HashMap<>();
    private int maxTick = -1;

    private long startTime = System.currentTimeMillis();
    private long stopTime = -1;

    public PlayerRecord(IPlayer player, Skin skin) {
        this.player = player;
        this.skin = skin;
    }

    public Map<Integer, PlayerRecordTick> getTicks() {
        return ticks;
    }

    public int getSize() {
        return this.ticks.size();
    }

    public void recordTick(int tick, PlayerRecordTick entry) {
        if (this.ticks.containsKey(tick - 1)) {
            if (!this.ticks.get(tick - 1).equals(entry)) {
                this.ticks.put(tick, entry);
            }
        } else {
            this.ticks.put(tick, entry);
        }
    }

    public PlayerRecordTick playBackTick(int tick) {
        if (this.ticks.containsKey(tick)) {
            return this.ticks.get(tick);
        } else {
            return null;
        }
    }

    public IPlayer getPlayer() {
        return player;
    }

    public Skin getSkin() {
        return skin;
    }

    public int getMaxTick() {
        return maxTick;
    }

    public void setMaxTick(int maxTick) {
        this.maxTick = maxTick;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getStopTime() {
        return stopTime;
    }

    public void setStopTime(long stopTime) {
        this.stopTime = stopTime;
    }
}
