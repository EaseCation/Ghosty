package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.inventory.ArmorInventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 17:02.
 * All rights reserved
 */
public class PlayerUpdatedArmor0 implements PlayerUpdated {

    private Item item;

    public static PlayerUpdatedArmor0 of(Item item) {
        return new PlayerUpdatedArmor0(item);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ARMOR_0;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getArmorInventory() != null) {
            ArmorInventory inv = ghost.getArmorInventory();
            inv.setItem(ArmorInventory.SLOT_HEAD, item == null ? Item.get(Item.AIR) : item);
            inv.sendContents(ghost.getViewers().values());
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setArmor0(item);
        return node;
    }

    public PlayerUpdatedArmor0(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 1, 2, 3: {
                this.item = PersistenceBinaryStreamHelper.getItem(stream);
                break;
            }
            case 0: {
                read(stream);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported format version: " + formatVersion);
        }
    }

    private PlayerUpdatedArmor0(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedArmor0 o)) return false;
        return (item.equals(o.item));
    }

    @Override
    public void write(BinaryStream stream) {
        PersistenceBinaryStreamHelper.putItem(stream, this.item);
    }

    @Override
    public void read(BinaryStream stream) {
        this.item = stream.getSlot();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedArmor0{" +
            "item=" + item +
            '}';
    }
}
