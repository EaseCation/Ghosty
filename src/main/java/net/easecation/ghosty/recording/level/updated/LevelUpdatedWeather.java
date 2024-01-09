package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedWeather implements LevelUpdated {

    private boolean rain;
    private int intensity;

    private LevelUpdatedWeather(boolean rain, int intensity) {
        this.rain = rain;
        this.intensity = intensity;
    }

    public LevelUpdatedWeather(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedWeather of(boolean rain, int intensity) {
        return new LevelUpdatedWeather(rain, intensity);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_WEATHER;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> level.setRaining(rain, intensity));
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putBoolean(this.rain);
        stream.putVarInt(this.intensity);
    }

    @Override
    public void read(BinaryStream stream) {
        this.rain = stream.getBoolean();
        this.intensity = stream.getVarInt();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedWeather o)) return false;
        return this.rain == o.rain && this.intensity == o.intensity;
    }

    @Override
    public String toString() {
        return "LevelUpdatedWeather{" +
            "rain=" + rain +
            ", intensity=" + intensity +
            '}';
    }

    @Override
    public int hashCode() {
        return Boolean.hashCode(rain) + Integer.hashCode(intensity);
    }
}
