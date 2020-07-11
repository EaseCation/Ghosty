package net.easecation.ghosty.recording;

import cn.nukkit.inventory.PlayerInventory;
import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 17:02.
 * All rights reserved
 */
class UpdatedItem implements Updated {

    private Item item;

    static UpdatedItem of(Item item) {
        return new UpdatedItem(item);
    }

    @Override
    public int getUpdateTypeId() {
        return Updated.TYPE_ITEM;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            PlayerInventory inv = ghost.getInventory();
            inv.setItemInHand(item == null ? Item.get(Item.AIR) : item);
            inv.sendHeldItem(ghost.getViewers().values());
        }
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setItem(item);
        return node;
    }

    public UpdatedItem(BinaryStream stream) {
        read(stream);
    }

    private UpdatedItem(Item item) {
        this.item = item;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedItem)) return false;
        UpdatedItem o = (UpdatedItem) obj;
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
}
