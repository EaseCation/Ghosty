package net.easecation.ghosty.recording;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import net.easecation.ghosty.entity.PlaybackNPC;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
class UpdatedDataFlags implements Updated {

    static UpdatedDataFlags of(long flags) {
        return new UpdatedDataFlags(flags);
    }

    private long flags;

    @Override
    public void processTo(PlaybackNPC ghost) {
        ghost.setDataProperty(new LongEntityData(Entity.DATA_FLAGS, this.flags));
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setDataFlags(flags);
        return node;
    }

    private UpdatedDataFlags(long flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedDataFlags)) return false;
        UpdatedDataFlags o = (UpdatedDataFlags) obj;
        return (Objects.equals(flags, o.flags));
    }
}
