package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class EntityUpdatedTagName implements EntityUpdated {

    @Override
    public int getUpdateTypeId() {
        return EntityUpdated.TYPE_TAG_NAME;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public static EntityUpdatedTagName of(String tn) {
        return new EntityUpdatedTagName(tn);
    }

    private String tn;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setNameTag(tn);
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setTagName(tn);
        return node;
    }

    public EntityUpdatedTagName(BinaryStream stream) {
        read(stream);
    }

    private EntityUpdatedTagName(String tn) {
        this.tn = tn;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedTagName o)) return false;
        return (Objects.equals(tn, o.tn));
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(tn);
    }

    @Override
    public void read(BinaryStream stream) {
        this.tn = stream.getString();
    }

    @Override
    public String toString() {
        return "EntityUpdatedTagName{" +
            "tn='" + tn + '\'' +
            '}';
    }
}
