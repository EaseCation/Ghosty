package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

public class PlayerUpdatedAnimate implements PlayerUpdated {

    private int action;
    private float rowingTime;

    public static PlayerUpdatedAnimate of(int action, float rowingTime) {
        return new PlayerUpdatedAnimate(action, rowingTime);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ANIMATE;
    }

    @Override
    public boolean hasStates() {
        return false;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            AnimatePacket pk = new AnimatePacket();
            pk.eid = ghost.getId();
            pk.action = AnimatePacket.Action.fromId(this.action);
            pk.rowingTime = this.rowingTime;
            Server.broadcastPacket(ghost.getViewers().values(), pk);
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        // 不需要在init时应用
        return node;
    }

    public PlayerUpdatedAnimate(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedAnimate(int action, float rowingTime) {
        this.action = action;
        this.rowingTime = rowingTime;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedAnimate o)) return false;
        return action == o.action && rowingTime == o.rowingTime;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putVarInt(this.action);
        stream.putLFloat(this.rowingTime);
    }

    @Override
    public void read(BinaryStream stream) {
        this.action = stream.getVarInt();
        this.rowingTime = stream.getLFloat();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedAnimate{" +
            "action=" + action +
            ", rowingTime=" + rowingTime +
            '}';
    }
}
