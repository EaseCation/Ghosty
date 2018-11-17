package net.easecation.ghosty.recording;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;

import java.io.Serializable;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:20.
 */
interface Updated {

    int TYPE_POSITION_XYZ = 0;
    int TYPE_ROTATION = 1;
    int TYPE_TAG_NAME = 2;
    int TYPE_WORLD = 3;
    int TYPE_DATA_FLAGS = 4;
    int TYPE_ITEM = 5;

    int getUpdateTypeId();

    void processTo(PlaybackNPC ghost);

    RecordNode applyTo(RecordNode node);

    void write(BinaryStream stream);

    void read(BinaryStream stream);

    static Updated fromBinaryStream(BinaryStream stream) {
        switch (stream.getByte()) {
            case TYPE_POSITION_XYZ:
                return new UpdatedPositionXYZ(stream);
            case TYPE_ROTATION:
                return new UpdatedRotation(stream);
            case TYPE_TAG_NAME:
                return new UpdatedTagName(stream);
            case TYPE_WORLD:
                return new UpdatedWorld(stream);
            case TYPE_DATA_FLAGS:
                return new UpdatedDataFlags(stream);
            case TYPE_ITEM:
                return new UpdatedItem(stream);
        }
        return null;
    }

}
