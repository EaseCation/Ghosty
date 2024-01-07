package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.Player;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedActionBar implements LevelUpdated {

    private String message;
    int fadeInTime;
    int stayTime;
    int fadeOutTime;

    private LevelUpdatedActionBar(String message, int fadeInTime, int stayTime, int fadeOutTime) {
        this.message = message;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
    }

    public LevelUpdatedActionBar(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedActionBar of(String message, int fadeInTime, int stayTime, int fadeOutTime) {
        return new LevelUpdatedActionBar(message, fadeInTime, stayTime, fadeOutTime);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_ACTION_BAR;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> {
            for (Player player : level.getPlayers().values()) {
                player.sendActionBar(this.message, this.fadeInTime, this.stayTime, this.fadeOutTime);
            }
        });
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(message);
        stream.putLInt(fadeInTime);
        stream.putLInt(stayTime);
        stream.putLInt(fadeOutTime);
    }

    @Override
    public void read(BinaryStream stream) {
        this.message = stream.getString();
        this.fadeInTime = stream.getLInt();
        this.stayTime = stream.getLInt();
        this.fadeOutTime = stream.getLInt();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedActionBar o)) return false;
        return this.message.equals(o.message) && this.fadeInTime == o.fadeInTime && this.stayTime == o.stayTime && this.fadeOutTime == o.fadeOutTime;
    }

    @Override
    public String toString() {
        return "LevelUpdatedActionBar{" +
            "message='" + message + '\'' +
            ", fadeInTime=" + fadeInTime +
            ", stayTime=" + stayTime +
            ", fadeOutTime=" + fadeOutTime +
            '}';
    }

    @Override
    public int hashCode() {
        return this.message.hashCode() + this.fadeInTime + this.stayTime + this.fadeOutTime;
    }
}
