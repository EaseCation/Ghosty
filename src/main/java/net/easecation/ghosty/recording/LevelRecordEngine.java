package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.LevelRecordPack;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.level.LevelRecordImpl;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.SkinlessPlayerRecord;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class LevelRecordEngine {

    private int tick = 0;
    private final Level level;
    private boolean recording = true;
    private final Map<Player, PlayerRecordEngine> playerRecordEngines = new HashMap<>();
    private final List<PlayerRecord> playerRecords = new ArrayList<>();
    private final LevelRecord levelRecord;
    private final LevelRecordNode levelRecordNode;
    private final TaskHandler taskHandler;
    private final int callbackIdBlockSet;
    private final int callbackIdChunkPacketSend;

    public LevelRecordEngine(Level level) {
        this.level = level;
        this.levelRecord = new LevelRecordImpl();
        this.levelRecordNode = new LevelRecordNode();
        this.callbackIdBlockSet = level.addCallbackBlockSet(this::onLevelBlockSet);
        this.callbackIdChunkPacketSend = level.addCallbackChunkPacketSend(this::onLevelChunkPacketSend);
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        GhostyPlugin.getInstance().recordingLevelEngines.put(level, this);
    }

    public void addPlayer(Player player) {
        PlayerRecordEngine recordEngine = new PlayerRecordEngine(player, SkinlessPlayerRecord::new);
        recordEngine.setUnifySave(false);
        recordEngine.setTick(this.tick + 1);  // 这边需要+1，因为玩家是从下一tick才开始录制的
        playerRecordEngines.put(player, recordEngine);
    }

    public void addPlayer(Player player, Function<Player, PlayerRecord> recordFactory) {
        PlayerRecordEngine recordEngine = new PlayerRecordEngine(player, recordFactory);
        recordEngine.setUnifySave(false);
        recordEngine.setTick(this.tick + 1);  // 这边需要+1，因为玩家是从下一tick才开始录制的
        playerRecordEngines.put(player, recordEngine);
    }

    public void removePlayer(Player player) {
        playerRecordEngines.remove(player);
    }

    public void onTick() {
        if (!this.recording) {
            return;
        }
        // 玩家录制器
        for (PlayerRecordEngine engine : playerRecordEngines.values()) {
            if (engine.isStopped()) {
                // 保存
                playerRecords.add(engine.getRecord());
            }
        }
        for (Player player : level.getPlayers().values()) {
            // 添加玩家到录制器中
            if (!playerRecordEngines.containsKey(player)) {
                this.addPlayer(player, SkinlessPlayerRecord::new);
            }
        }
        playerRecordEngines.entrySet().removeIf(e -> e.getValue().isStopped());
        // Level录制器
        this.levelRecord.record(this.tick, this.levelRecordNode);
        this.tick++;
    }

    public void stopRecord() {
        this.recording = false;
        for (PlayerRecordEngine engine : playerRecordEngines.values()) {
            PlayerRecord playerRecord = engine.stopRecord();
            this.playerRecords.add(playerRecord);
        }
        this.playerRecordEngines.clear();
        this.taskHandler.cancel();
        this.level.removeCallbackBlockSet(this.callbackIdBlockSet);
        this.level.removeCallbackChunkPacketSend(this.callbackIdChunkPacketSend);
    }

    public LevelRecordPack toRecordPack() {
        return new LevelRecordPack(this.levelRecord, this.playerRecords);
    }

    public Level getLevel() {
        return level;
    }

    public boolean isRecording() {
        return recording;
    }

    public List<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

    public Map<Player, PlayerRecordEngine> getPlayerRecordEngines() {
        return playerRecordEngines;
    }

    public LevelRecord getLevelRecord() {
        return levelRecord;
    }

    /**
     * 注册到Level的回调：方块被更改
     * @param block Block
     */
    public void onLevelBlockSet(Block block) {
        if (!this.recording) {
            return;
        }
        this.levelRecordNode.handleBlockChange(block.asBlockVector3(), block);
    }

    /**
     * 注册到Level的回调：发送区块数据包
     * @param chunkIndex 区块索引
     * @param packet DataPacket
     */
    public void onLevelChunkPacketSend(long chunkIndex, DataPacket packet) {
        if (!this.recording) {
            return;
        }
        switch (packet.pid()) {
            case ProtocolInfo.LEVEL_EVENT_PACKET:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3:
            case ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET:
            case ProtocolInfo.PLAY_SOUND_PACKET:
            case ProtocolInfo.STOP_SOUND_PACKET:
            case ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET:
                this.levelRecordNode.handleLevelChunkPacket(chunkIndex, packet);
                break;
        }
    }

    public void onEntitySpawn(Entity entity) {
        if (!this.recording) {
            return;
        }
        this.levelRecordNode.handleEntitySpawn(entity);
    }

    public void onEntityDespawn(Entity entity) {
        if (!this.recording) {
            return;
        }
        this.levelRecordNode.handleEntityDespawn(entity);
    }

}
