package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedPlaySound implements LevelUpdated {

    private PlaySoundPacket packet;

    private LevelUpdatedPlaySound(PlaySoundPacket packet) {
        this.packet = packet;
    }

    public static LevelUpdatedPlaySound of(PlaySoundPacket packet) {
        return new LevelUpdatedPlaySound(packet);
    }

    public LevelUpdatedPlaySound(BinaryStream stream) {
        this.read(stream);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_PLAY_SOUND;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.handleLevelChunkPacket(Level.chunkHash(packet.x >> 4, packet.z >> 4), packet);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(packet.name);
        stream.putBlockVector3(new BlockVector3(packet.x, packet.y, packet.z));
        stream.putLFloat(packet.volume);
        stream.putLFloat(packet.pitch);
    }

    @Override
    public void read(BinaryStream stream) {
        this.packet = new PlaySoundPacket();
        this.packet.name = stream.getString();
        BlockVector3 pos = stream.getBlockVector3();
        this.packet.x = pos.x;
        this.packet.y = pos.y;
        this.packet.z = pos.z;
        this.packet.volume = stream.getLFloat();
        this.packet.pitch = stream.getLFloat();
    }

    @Override
    public String toString() {
        return "LevelUpdatedPlaySound{" +
            "packet=" + packet +
            '}';
    }
}