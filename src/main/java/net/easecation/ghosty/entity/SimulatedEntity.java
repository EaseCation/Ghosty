package net.easecation.ghosty.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.EntityID;
import cn.nukkit.item.Item;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.network.protocol.AddEntityPacket;
import cn.nukkit.network.protocol.AddItemEntityPacket;
import cn.nukkit.network.protocol.AddPlayerPacket;
import cn.nukkit.network.protocol.DataPacket;

import java.util.UUID;

public class SimulatedEntity extends Entity {

    private final int networkId;
    private final String entityIdentifier;
    private final long originEid;

    public Item item = null;
    private UUID uuid = null;

    public SimulatedEntity(FullChunk chunk, CompoundTag nbt, int networkId, String entityIdentifier, long originEid) {
        super(chunk, nbt);
        this.networkId = networkId;
        this.entityIdentifier = entityIdentifier;
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
        if (this.networkId == EntityID.ITEM) {
            return 0.125f;
        } else if (this.networkId == -1 || this.networkId == EntityID.PLAYER) {
            return 1.62f;
        } else if (this.networkId == EntityID.TNT) {
            return 0.49f;
        }
        return super.getBaseOffset();
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

    public UUID getUniqueId() {
        if (this.uuid == null) {
            this.uuid = UUID.randomUUID();
        }
        return this.uuid;
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
        } else if (networkId == -1 || networkId == EntityID.PLAYER) {
            AddPlayerPacket pk = new AddPlayerPacket();
            pk.uuid = this.getUniqueId();
            pk.username = this.getNameTag();
            pk.entityUniqueId = this.getId();
            pk.entityRuntimeId = this.getId();
            pk.x = (float) this.x;
            pk.y = (float) this.y;
            pk.z = (float) this.z;
            pk.speedX = (float) this.motionX;
            pk.speedY = (float) this.motionY;
            pk.speedZ = (float) this.motionZ;
            pk.yaw = (float) this.yaw;
            pk.headYaw = (float) this.yaw;
            pk.pitch = (float) this.pitch;
            pk.item = this.item;
            pk.metadata = this.dataProperties;
            return pk;
        } else if (networkId == 0 && !this.entityIdentifier.isEmpty()) {
            DataPacket addEntityPacket = super.createAddEntityPacket();
            ((AddEntityPacket) addEntityPacket).id = this.entityIdentifier;
            return addEntityPacket;
        } else {
            return super.createAddEntityPacket();
        }
    }

    @Override
    public void close() {
        super.close();
    }
}
