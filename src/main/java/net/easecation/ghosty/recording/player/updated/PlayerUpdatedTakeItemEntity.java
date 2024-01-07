package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.network.protocol.TakeItemEntityPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

public class PlayerUpdatedTakeItemEntity implements PlayerUpdated {

    private long targetEntityId;

    public static PlayerUpdatedTakeItemEntity of(long targetEntityId) {
        return new PlayerUpdatedTakeItemEntity(targetEntityId);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_TAKE_ITEM_ENTITY;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null) {
            long target = -1;
            for (Entity entity : ghost.getLevel().getEntities()) {
                if (entity instanceof SimulatedEntity sim && sim.getOriginEid() == targetEntityId) {
                    target = sim.getId();
                    break;
                }
            }
            if (target != -1) {
                TakeItemEntityPacket pk = new TakeItemEntityPacket();
                pk.entityId = ghost.getId();
                pk.target = target;
                Server.broadcastPacket(ghost.getViewers().values(), pk);
            }
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        // 不需要在init时应用
        return node;
    }

    public PlayerUpdatedTakeItemEntity(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedTakeItemEntity(long targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedTakeItemEntity o)) return false;
        return this.targetEntityId == o.targetEntityId;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putEntityUniqueId(targetEntityId);
    }

    @Override
    public void read(BinaryStream stream) {
        targetEntityId = stream.getEntityUniqueId();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedTakeItemEntity{" +
                "targetEntityId=" + targetEntityId +
                '}';
    }
}
