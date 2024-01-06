package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 17:02.
 * All rights reserved
 */
public class PlayerUpdatedArmor1 implements PlayerUpdated {

    private Item item;

    public static PlayerUpdatedArmor1 of(Item item) {
        return new PlayerUpdatedArmor1(item);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ARMOR_1;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            PlayerInventory inv = ghost.getInventory();
            inv.setArmorItem(1, item == null ? Item.get(Item.AIR) : item);
            inv.sendArmorContents(ghost.getViewers().values());
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setArmor1(item);
        return node;
    }

    public PlayerUpdatedArmor1(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedArmor1(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedArmor1 o)) return false;
        return (item.equals(o.item));
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putSlot(this.item);
    }

    @Override
    public void read(BinaryStream stream) {
        this.item = stream.getSlot();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedArmor1{" +
            "item=" + item +
            '}';
    }
}
