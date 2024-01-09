package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Server;
import cn.nukkit.level.Location;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class PlayerUpdatedWorldChanged implements PlayerUpdated {

    @Override
    public int getUpdateTypeId() {
        return TYPE_WORLD;
    }

    public static PlayerUpdatedWorldChanged of(String tn) {
        return new PlayerUpdatedWorldChanged(tn);
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    private String wn;

    @Override
    public void processTo(PlaybackNPC ghost) {
        Location location = ghost.getLocation();
        location.level = Server.getInstance().getLevelByName(wn);
        if (location.level == null) return;
        // ghost.teleport(location);
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setLevel(wn);
        return node;
    }

    public PlayerUpdatedWorldChanged(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedWorldChanged(String wn) {
        this.wn = wn;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PlayerUpdatedWorldChanged o)) return false;
        return (Objects.equals(wn, o.wn));
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(wn);
    }

    @Override
    public void read(BinaryStream stream) {
        wn = stream.getString();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedWorldChanged{" +
            "wn='" + wn + '\'' +
            '}';
    }
}
