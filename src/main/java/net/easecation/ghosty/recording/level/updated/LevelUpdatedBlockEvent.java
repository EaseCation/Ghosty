package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.BlockEventPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedBlockEvent implements LevelUpdated {

    public int x;
    public int y;
    public int z;
    public int eventType;
    public int eventData;

    private LevelUpdatedBlockEvent(int evid, float x, float y, float z, int data) {
        this.x = (int) x;
        this.y = (int) y;
        this.z = (int) z;
        this.eventType = evid;
        this.eventData = data;
    }

    public static LevelUpdatedBlockEvent of(BlockEventPacket packet) {
        return new LevelUpdatedBlockEvent(packet.eventType, packet.x, packet.y, packet.z, packet.eventData);
    }

    public LevelUpdatedBlockEvent(BinaryStream stream) {
        this.read(stream);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_BLOCK_EVENT;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        BlockEventPacket packet = new BlockEventPacket();
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        packet.eventType = this.eventType;
        packet.eventData = this.eventData;
        node.handleLevelChunkPacket(Level.chunkHash(x >> 4, z >> 4), packet);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putBlockVector3(this.x, this.y, this.z);
        stream.putVarInt(this.eventType);
        stream.putVarInt(this.eventData);
    }

    @Override
    public void read(BinaryStream stream) {
        BlockVector3 pos = stream.getBlockVector3();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.eventType = stream.getVarInt();
        this.eventData = stream.getVarInt();
    }

    @Override
    public String toString() {
        return "LevelUpdatedBlockEvent{" +
            "x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", eventType=" + eventType +
            ", eventData=" + eventData +
            '}';
    }
}
