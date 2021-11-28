package net.easecation.ghosty.entity;

import cn.nukkit.Player;
import cn.nukkit.entity.EntityHuman;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.entity.EntityDamageEvent;
import cn.nukkit.inventory.InventoryHolder;
import cn.nukkit.level.Location;
import cn.nukkit.level.format.FullChunk;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.nbt.tag.DoubleTag;
import cn.nukkit.nbt.tag.FloatTag;
import cn.nukkit.nbt.tag.ListTag;
import cn.nukkit.network.protocol.AddPlayerPacket;

import java.util.List;

public class PlaybackNPC extends EntityHuman implements InventoryHolder {

    public static Skin defaultSkin;
    private final List<Player> viewers;

    public PlaybackNPC(FullChunk chunk, CompoundTag nbt, Skin skin, String name, List<Player> viewers) {
        super(chunk, nbt);
        this.setSkin(skin == null ? defaultSkin : skin);
        this.setNameTagVisible(true);
        this.setNameTagAlwaysVisible(true);
        this.getInventory().setHeldItemSlot(0);
        this.setNameTag(name);
        this.viewers = viewers;
    }

    public PlaybackNPC(Location pos, Skin skin, String name, List<Player> viewers){
        this(pos.getLevel().getChunk(pos.getFloorX() >> 4, pos.getFloorZ() >> 4),
                new CompoundTag()
                        .putList(new ListTag<DoubleTag>("Pos")
                                .add(new DoubleTag("", pos.x))
                                .add(new DoubleTag("", pos.y))
                                .add(new DoubleTag("", pos.z)))
                        .putList(new ListTag<DoubleTag>("Motion")
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0))
                                .add(new DoubleTag("", 0)))
                        .putList(new ListTag<FloatTag>("Rotation")
                                        .add(new FloatTag("", (float)pos.yaw))
                                        .add(new FloatTag("", (float)pos.pitch))
                        ), skin, name, viewers);
        this.saveNBT();
    }

    @Override
    public boolean attack(EntityDamageEvent source) {
        return false;
    }

    @Override
    public void spawnTo(Player player) {
        if((this.viewers == null || this.viewers.contains(player)) && !this.hasSpawned.containsKey(player.getLoaderId())) {
            this.hasSpawned.put(player.getLoaderId(), player);

            if (!this.skin.isValid()) {
                throw new IllegalStateException(this.getClass().getSimpleName() + " must have a valid skin set");
            }
            this.server.updatePlayerListData(this.getUniqueId(), this.getId(), this.getName(), this.skin, new Player[]{player});
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
            pk.item = this.getInventory().getItemInHand();
            pk.metadata = this.dataProperties;
            player.dataPacket(pk);
            this.inventory.sendArmorContents(player);
            this.getInventory().sendHeldItem(player);
        }
    }

    @Override
    public void despawnFrom(Player player) {
        super.despawnFrom(player);
        this.server.removePlayerListData(this.getUniqueId());
    }

    @Override
    public void kill() {
        if (this.getInventory() != null) this.getInventory().clearAll();
        super.kill();
    }


    public void resendPosition() {
        this.getLevel().addEntityMovement(this.chunk.getX(), this.chunk.getZ(), this.getId(), this.x, y, this.z, this.yaw, this.pitch, this.yaw);
    }

    @Override
    public Skin getSkin() {
        return super.getSkin() == null ? defaultSkin : super.getSkin();
    }

}
