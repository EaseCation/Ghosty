package net.easecation.ghosty.recording;

import cn.nukkit.item.Item;
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
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) ghost.getInventory().setItemInHand(item == null ? Item.get(0) : item);
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setItem(item);
        return node;
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
}
