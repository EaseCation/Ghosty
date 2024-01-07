package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedPlaySound implements LevelUpdated {

    public String name;
    public int x;
    public int y;
    public int z;
    public float volume;
    public float pitch;

    private LevelUpdatedPlaySound(String name, int x, int y, int z, float volume, float pitch) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.volume = volume;
        this.pitch = pitch;
    }

    public static LevelUpdatedPlaySound of(PlaySoundPacket packet) {
        return new LevelUpdatedPlaySound(packet.name, packet.x, packet.y, packet.z, packet.volume, packet.pitch);
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
        PlaySoundPacket packet = new PlaySoundPacket();
        packet.name = this.name;
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        packet.volume = this.volume;
        packet.pitch = this.pitch;
        node.handleLevelChunkPacket(Level.chunkHash(this.x >> 4, this.z >> 4), packet);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(name);
        stream.putBlockVector3(new BlockVector3(x, y, z));
        stream.putLFloat(volume);
        stream.putLFloat(pitch);
    }

    @Override
    public void read(BinaryStream stream) {
        this.name = stream.getString();
        BlockVector3 pos = stream.getBlockVector3();
        this.x = pos.x;
        this.y = pos.y;
        this.z = pos.z;
        this.volume = stream.getLFloat();
        this.pitch = stream.getLFloat();
    }

    @Override
    public String toString() {
        return "LevelUpdatedPlaySound{" +
            "name='" + name + '\'' +
            ", x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", volume=" + volume +
            ", pitch=" + pitch +
            '}';
    }
}