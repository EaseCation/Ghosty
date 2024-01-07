package net.easecation.ghosty.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddItemEntityPacket;
import cn.nukkit.network.protocol.DataPacket;

public class SimulatedEntity extends Entity {

    private final int networkId;
    private final long originEid;

    public Item item = null;

    public SimulatedEntity(FullChunk chunk, CompoundTag nbt, int networkId, long originEid) {
        super(chunk, nbt);
        this.networkId = networkId;
        this.originEid = originEid;
        this.needEntityBaseTick = false;
    }

    @Override
    public int getNetworkId() {
        return networkId;
    }

    public long getOriginEid() {
        return originEid;
    }

    @Override
    protected float getBaseOffset() {
        return this.networkId == EntityID.ITEM ? 0.125f : super.getBaseOffset();
    }

    @Override
    public void spawnTo(Player player) {
        if (this.hasSpawned.containsKey(player.getLoaderId())) {
            return;
        }
        DataPacket pk = this.createAddEntityPacket();
        player.dataPacket(pk);
        super.spawnTo(player);
    }

    @Override
    protected DataPacket createAddEntityPacket() {
        if (networkId == EntityID.ITEM) {
            AddItemEntityPacket addEntity = new AddItemEntityPacket();
            addEntity.entityUniqueId = this.getId();
            addEntity.entityRuntimeId = this.getId();
            addEntity.x = (float) this.x;
            addEntity.y = (float) this.y + this.getBaseOffset();
            addEntity.z = (float) this.z;
            addEntity.speedX = (float) this.motionX;
            addEntity.speedY = (float) this.motionY;
            addEntity.speedZ = (float) this.motionZ;
            addEntity.metadata = this.dataProperties;
            addEntity.item = this.item;
            return addEntity;
        } else {
            return super.createAddEntityPacket();
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
