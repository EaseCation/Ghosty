package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.level.Level;
import cn.nukkit.math.Vector3f;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LevelSoundEventPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.LevelSoundEventPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacket18;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacketV218;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.LevelSoundEventPacketV319;

public class LevelUpdatedLevelSoundEvent implements LevelUpdated {

    public int sound;
    public float x;
    public float y;
    public float z;
    public int extraData = -1;
    public String entityIdentifier = ":";
    public boolean isBabyMob;
    public boolean isGlobal;

    private LevelUpdatedLevelSoundEvent(int sound, float x, float y, float z, int extraData, String entityIdentifier, boolean isBabyMob, boolean isGlobal) {
        this.sound = sound;
        this.x = x;
        this.y = y;
        this.z = z;
        this.extraData = extraData;
        this.entityIdentifier = entityIdentifier;
        this.isBabyMob = isBabyMob;
        this.isGlobal = isGlobal;
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacket packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal);
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacket14 packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, ":", packet.isBabyMob, packet.isGlobal);
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacket16 packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, ":", packet.isBabyMob, packet.isGlobal);
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacket18 packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, ":", packet.isBabyMob, packet.isGlobal);
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacketV218 packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal);
    }

    public static LevelUpdatedLevelSoundEvent of(LevelSoundEventPacketV319 packet) {
        return new LevelUpdatedLevelSoundEvent(packet.sound, packet.x, packet.y, packet.z, packet.extraData, packet.entityIdentifier, packet.isBabyMob, packet.isGlobal);
    }

    public LevelUpdatedLevelSoundEvent(BinaryStream stream) {
        this.read(stream);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_LEVEL_SOUND_EVENT;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        LevelSoundEventPacket packet = new LevelSoundEventPacket();
        packet.sound = this.sound;
        packet.x = this.x;
        packet.y = this.y;
        packet.z = this.z;
        packet.extraData = this.extraData;
        packet.entityIdentifier = this.entityIdentifier;
        packet.isBabyMob = this.isBabyMob;
        packet.isGlobal = this.isGlobal;
        node.handleLevelChunkPacket(Level.chunkHash((int) x >> 4, (int) z >> 4), packet);
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putUnsignedVarInt(sound);
        stream.putVector3f(x, y, z);
        stream.putVarInt(extraData);
        stream.putString(entityIdentifier);
        stream.putBoolean(isBabyMob);
        stream.putBoolean(isGlobal);
    }

    @Override
    public void read(BinaryStream stream) {
        this.sound = (int) stream.getUnsignedVarInt();
        Vector3f v = stream.getVector3f();
        this.x = v.x;
        this.y = v.y;
        this.z = v.z;
        this.extraData = stream.getVarInt();
        this.entityIdentifier = stream.getString();
        this.isBabyMob = stream.getBoolean();
        this.isGlobal = stream.getBoolean();
    }

    @Override
    public String toString() {
        return "LevelUpdatedLevelSoundEvent{" +
            "sound=" + sound +
            ", x=" + x +
            ", y=" + y +
            ", z=" + z +
            ", extraData=" + extraData +
            ", entityIdentifier='" + entityIdentifier + '\'' +
            ", isBabyMob=" + isBabyMob +
            ", isGlobal=" + isGlobal +
            '}';
    }
}