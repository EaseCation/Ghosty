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
public class PlayerUpdatedArmor2 implements PlayerUpdated {

    private Item item;

    public static PlayerUpdatedArmor2 of(Item item) {
        return new PlayerUpdatedArmor2(item);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ARMOR_2;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            PlayerInventory inv = ghost.getInventory();
            inv.setArmorItem(2, item == null ? Item.get(Item.AIR) : item);
            inv.sendArmorContents(ghost.getViewers().values());
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setArmor2(item);
        return node;
    }

    public PlayerUpdatedArmor2(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedArmor2(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedArmor2 o)) return false;
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
        return "PlayerUpdatedArmor2{" +
            "item=" + item +
            '}';
    }
}
