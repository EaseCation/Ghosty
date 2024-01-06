package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Server;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

public class PlayerUpdatedEntityEvent implements PlayerUpdated {

    private int event;
    private int data;

    public static PlayerUpdatedEntityEvent of(int event, int data) {
        return new PlayerUpdatedEntityEvent(event, data);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ANIMATE;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = ghost.getId();
            pk.event = this.event;
            pk.data = this.data;
            Server.broadcastPacket(ghost.getViewers().values(), pk);
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        // 不需要在init时应用
        return node;
    }

    public PlayerUpdatedEntityEvent(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedEntityEvent(int event, int data) {
        this.event = event;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedEntityEvent o)) return false;
        return event == o.event && data == o.data;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putByte((byte) this.event);
        stream.putVarInt(this.data);
    }

    @Override
    public void read(BinaryStream stream) {
        this.event = stream.getByte();
        this.data = stream.getVarInt();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedEntityEvent{" +
            "event=" + event +
            ", data=" + data +
            '}';
    }
}
