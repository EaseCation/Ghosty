package net.easecation.ghosty.recording.entity.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecordNode;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
public class EntityUpdatedSkinInfo implements EntityUpdated {

    @Override
    public int getUpdateTypeId() {
        return EntityUpdated.TYPE_SKIN_INFO;
    }

    @Override
    public boolean hasStates() {
        return true;
    }

    public static EntityUpdatedSkinInfo of(String geometryName, String skinDataHash) {
        return new EntityUpdatedSkinInfo(geometryName, skinDataHash);
    }

    private String geometryName;
    private String skinDataHash;

    @Override
    public void processTo(SimulatedEntity entity) {
        entity.setSkinInfo(new SimulatedEntity.SkinInfo(geometryName, skinDataHash));
    }

    @Override
    public EntityRecordNode applyTo(EntityRecordNode node) {
        node.setSkinInfo(new SimulatedEntity.SkinInfo(geometryName, skinDataHash));
        return node;
    }

    public EntityUpdatedSkinInfo(BinaryStream stream) {
        read(stream);
    }

    private EntityUpdatedSkinInfo(String geometryName, String skinDataHash) {
        this.geometryName = geometryName;
        this.skinDataHash = skinDataHash;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof EntityUpdatedSkinInfo o)) return false;
        return (Objects.equals(geometryName, o.geometryName));
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(geometryName);
        stream.putString(skinDataHash);
    }

    @Override
    public void read(BinaryStream stream) {
        this.geometryName = stream.getString();
        this.skinDataHash = stream.getString();
    }

    @Override
    public String toString() {
        return "EntityUpdatedSkinGeometryName{" +
            "geometryName='" + geometryName + '\'' +
            ", skinDataHash='" + skinDataHash + '\'' +
            '}';
    }
}
