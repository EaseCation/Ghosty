package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.Player;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedPopup implements LevelUpdated {

    private String message;

    private LevelUpdatedPopup(String message) {
        this.message = message;
    }

    public LevelUpdatedPopup(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedPopup of(String message) {
        return new LevelUpdatedPopup(message);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_POPUP;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> {
            for (Player player : level.getPlayers().values()) {
                player.sendPopup(this.message);
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
        if(!(obj instanceof LevelUpdatedPopup o)) return false;
        return this.message.equals(o.message);
    }

    @Override
    public String toString() {
        return "LevelUpdatedPopup{" +
            "message='" + message + '\'' +
            '}';
    }

    @Override
    public int hashCode() {
        return this.message.hashCode();
    }
}
