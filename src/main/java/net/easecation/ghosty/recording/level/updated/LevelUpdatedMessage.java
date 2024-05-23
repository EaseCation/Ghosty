package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.Player;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedMessage implements LevelUpdated {

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    private LevelUpdatedMessage(String message) {
        this.message = message;
    }

    public LevelUpdatedMessage(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedMessage of(String message) {
        return new LevelUpdatedMessage(message);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_MESSAGE;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> {
            for (Player player : level.getPlayers().values()) {
                player.sendMessage(this.message);
            }
        });
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(message);
    }

    @Override
    public void read(BinaryStream stream) {
        this.message = stream.getString();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedMessage o)) return false;
        return this.message.equals(o.message);
    }

    @Override
    public String toString() {
        return "LevelUpdatedMessage{" +
            "message='" + message + '\'' +
            '}';
    }

    @Override
    public int hashCode() {
        return this.message.hashCode();
    }
}
