package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class PlayerUpdatedDataFlags implements PlayerUpdated {

    public static PlayerUpdatedDataFlags of(long flags) {
        return new PlayerUpdatedDataFlags(flags);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_DATA_FLAGS;
    }

    private long flags;

    @Override
    public void processTo(PlaybackNPC ghost) {
        ghost.setDataProperty(new LongEntityData(Entity.DATA_FLAGS, this.flags));
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        node.setDataFlags(flags);
        return node;
    }

    public PlayerUpdatedDataFlags(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedDataFlags(long flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedDataFlags o)) return false;
        return flags == o.flags;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putVarLong(this.flags);
    }

    @Override
    public void read(BinaryStream stream) {
        this.flags = stream.getVarLong();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedDataFlags{" +
            "flags=" + flags +
            '}';
    }
}
