package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.TextFormat;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 17:02.
 * All rights reserved
 */
public class PlayerUpdatedPing implements PlayerUpdated {

    private int ping;

    public static PlayerUpdatedPing of(int ping) {
        return new PlayerUpdatedPing(ping);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_PING;
    }

    @Override
    public boolean hasStates() {
        return false;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        ghost.lastPing = this.ping;
        if (ghost.getEngine().displayPlayerPing) {
            String[] split = ghost.getNameTag().split("\n");
            ghost.setNameTag(getDisplayPing(this.ping) + TextFormat.WHITE + "\n" + split[split.length - 1]);
        }
    }

    public static String getDisplayPing(int ping) {
        if (ping < 0) {
            return "N/A";
        } else if (ping < 200) {
            return TextFormat.GREEN.toString() + ping + "ms";
        } else if (ping < 500) {
            return TextFormat.YELLOW.toString() + ping + "ms";
        } else {
            return TextFormat.RED.toString() + ping + "ms";
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setPing(this.ping);
        return node;
    }

    public PlayerUpdatedPing(BinaryStream stream) {
        this.ping = (int) stream.getUnsignedVarInt();
    }

    private PlayerUpdatedPing(int ping) {
        this.ping = ping;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedPing o)) return false;
        return ping == o.ping;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putUnsignedVarInt(this.ping);
    }

    @Override
    public void read(BinaryStream stream) {
        this.ping = (int) stream.getUnsignedVarInt();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedPing{" +
                "ping=" + ping +
                '}';
    }
}
