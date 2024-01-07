package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class EntityUpdatedDataFlags implements EntityUpdated {

    public static EntityUpdatedDataFlags of(long flags) {
        return new EntityUpdatedDataFlags(flags);
    }

    @Override
    public int getUpdateTypeId() {
        return TYPE_DATA_FLAGS;
    }

    private long flags;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setDataProperty(new LongEntityData(Entity.DATA_FLAGS, this.flags));
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setDataFlags(flags);
        return node;
    }

    public EntityUpdatedDataFlags(BinaryStream stream) {
        read(stream);
    }

    private EntityUpdatedDataFlags(long flags) {
        this.flags = flags;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedDataFlags o)) return false;
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
        return "EntityUpdatedDataFlags{" +
            "flags=" + flags +
            '}';
    }
}
