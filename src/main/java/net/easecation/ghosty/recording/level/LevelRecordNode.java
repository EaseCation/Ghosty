package net.easecation.ghosty.recording.level;

import cn.nukkit.block.Block;
import cn.nukkit.level.Level;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.LevelEventPacket;
import cn.nukkit.network.protocol.LevelSoundEventPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.easecation.ghosty.recording.level.updated.*;
import org.itxtech.synapseapi.multiprotocol.protocol112.protocol.LevelEventPacket112;
import org.itxtech.synapseapi.multiprotocol.protocol116100.protocol.LevelEventPacket116100;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LevelEventPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol14.protocol.LevelSoundEventPacket14;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.LevelEventPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol16.protocol.LevelSoundEventPacket16;
import org.itxtech.synapseapi.multiprotocol.protocol17.protocol.LevelEventPacket17;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacket18;
import org.itxtech.synapseapi.multiprotocol.protocol18.protocol.LevelSoundEventPacketV218;
import org.itxtech.synapseapi.multiprotocol.protocol19.protocol.LevelSoundEventPacketV319;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class LevelRecordNode {

    private final Map<BlockVector3, Block> blockChanges = new HashMap<>();
    private final Long2ObjectMap<List<DataPacket>> levelChunkPackets = new Long2ObjectOpenHashMap<>();

    public void clear() {
        blockChanges.clear();
        levelChunkPackets.clear();
    }

    public List<LevelUpdated> toUpdated() {
        List<LevelUpdated> list = new ArrayList<>();
        for (Map.Entry<BlockVector3, Block> entry : blockChanges.entrySet()) {
            list.add(LevelUpdatedBlockChange.of(entry.getKey(), entry.getValue()));
        }
        for (Map.Entry<Long, List<DataPacket>> entry : levelChunkPackets.long2ObjectEntrySet()) {
            for (DataPacket packet : entry.getValue()) {
                // LevelEventPacket
                if (packet instanceof LevelEventPacket pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                } else if (packet instanceof LevelEventPacket14 pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                } else if (packet instanceof LevelEventPacket16 pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                } else if (packet instanceof LevelEventPacket17 pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                } else if (packet instanceof LevelEventPacket112 pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                } else if (packet instanceof LevelEventPacket116100 pk) {
                    list.add(LevelUpdatedLevelEvent.of(pk));
                }
                // LevelSoundEventPacket
                else if (packet instanceof LevelSoundEventPacket pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof LevelSoundEventPacket14 pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof LevelSoundEventPacket16 pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof LevelSoundEventPacket18 pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof LevelSoundEventPacketV218 pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof LevelSoundEventPacketV319 pk) {
                    list.add(LevelUpdatedLevelSoundEvent.of(pk));
                } else if (packet instanceof PlaySoundPacket pk) {
                    list.add(LevelUpdatedPlaySound.of(pk));
                }
            }
        }
        return list;
    }

    public void applyToLevel(Level level) {
        for (Map.Entry<BlockVector3, Block> entry : blockChanges.entrySet()) {
            level.setBlock(entry.getKey(), entry.getValue(), true, false);
        }
        for (Map.Entry<Long, List<DataPacket>> entry : levelChunkPackets.long2ObjectEntrySet()) {
            int chunkX = Level.getHashX(entry.getKey());
            int chunkZ = Level.getHashZ(entry.getKey());
            for (DataPacket pk : entry.getValue()) {
                level.addChunkPacket(chunkX, chunkZ, pk);
            }
        }
    }

    public void handleBlockChange(BlockVector3 pos, Block block) {
        blockChanges.put(pos, block);
        // GhostyPlugin.getInstance().getLogger().debug("Block change: " + pos + " -> " + block);
    }

    private static final Long2ObjectFunction<List<DataPacket>> CHUNK_PACKET_MAPPING_FUNCTION = k -> new ArrayList<>();

    public void handleLevelChunkPacket(long chunkIndex, DataPacket pk) {
        levelChunkPackets.computeIfAbsent(chunkIndex, CHUNK_PACKET_MAPPING_FUNCTION).add(pk);
    }

}