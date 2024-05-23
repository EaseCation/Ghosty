package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class PlayerUpdatedTagName implements PlayerUpdated {

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_TAG_NAME;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public static PlayerUpdatedTagName of(String tn) {
        return new PlayerUpdatedTagName(tn);
    }

    private String tn;

    public void setTn(String tn) {
        this.tn = tn;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        ghost.setNameTag(tn);
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setTagName(tn);
        return node;
    }

    public PlayerUpdatedTagName(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedTagName(String tn) {
        this.tn = tn;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedTagName o)) return false;
        return (Objects.equals(tn, o.tn));
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(tn);
    }

    @Override
    public void read(BinaryStream stream) {
        this.tn = stream.getString();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedTagName{" +
            "tn='" + tn + '\'' +
            '}';
    }
}
