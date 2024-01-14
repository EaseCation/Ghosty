package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.block.Block;
import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.sound.SoundEnum;
import cn.nukkit.math.Vector3;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.PlaySoundPacket;
import cn.nukkit.network.protocol.ProtocolInfo;
import cn.nukkit.scheduler.TaskHandler;
import com.google.gson.JsonObject;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.LevelRecordPack;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.entity.EntityRecordImpl;
import net.easecation.ghosty.recording.entity.EntityRecordNode;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.level.LevelRecordImpl;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.recording.level.updated.*;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.SkinlessPlayerRecord;

import java.util.*;
import java.util.function.Function;

public class LevelRecordEngine {

    private int tick = 0;
    private final Level level;
    private int lastTime = -1;
    private boolean recording = true;
    private final Map<Player, PlayerRecordEngine> playerRecordEngines = new HashMap<>();
    private final List<PlayerRecord> playerRecords = new ArrayList<>();
    private final LevelRecord levelRecord;
    private final LevelRecordNode levelRecordNode;
    private final Map<Entity, EntityRecord> entityRecords = new HashMap<>();
    private final List<EntityRecord> closedEntityRecords = new ArrayList<>();

    private final TaskHandler taskHandler;
    private final int callbackIdBlockSet;
    private final int callbackIdChunkPacketSend;

    public LevelRecordEngine(Level level) {
        this.level = level;
        this.levelRecord = new LevelRecordImpl();
        this.levelRecordNode = new LevelRecordNode();
        // 初次录制时间
        this.checkTimeRecord();
        this.callbackIdBlockSet = level.addCallbackBlockSet(this::onLevelBlockSet);
        this.callbackIdChunkPacketSend = level.addCallbackChunkPacketSend(this::onLevelChunkPacketSend);
        // 初始化实体录制
        for (Entity entity : level.getEntities()) {
            this.onEntitySpawn(entity);
        }
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        GhostyPlugin.getInstance().recordingLevelEngines.put(level, this);
    }

    public void checkTimeRecord() {
        if (this.lastTime != this.level.getTime()) {
            this.lastTime = this.level.getTime();
            this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedTime.of(this.lastTime));
        }
    }

    public boolean isEntityNeedRecord(Entity entity) {
        /*return entity.getNetworkId() == EntityID.ITEM
            || entity.getNetworkId() == EntityID.ARMOR_STAND
            || entity.getNetworkId() == EntityID.SNOWBALL
            || entity.getNetworkId() == EntityID.ARROW
            || entity.getNetworkId() == EntityID.EGG
            || entity.getNetworkId() == EntityID.FIREBALL
            || entity.getNetworkId() == EntityID.DRAGON_FIREBALL
            || entity.getNetworkId() == EntityID.SMALL_FIREBALL
            || entity.getNetworkId() == EntityID.TNT
            || entity.getNetworkId() == EntityID.MINECART;*/
        return !(entity instanceof Player);
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
        this.checkTimeRecord();
        this.levelRecord.record(this.tick, this.levelRecordNode);
        // Entity录制器
        Iterator<Map.Entry<Entity, EntityRecord>> iterator = this.entityRecords.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<Entity, EntityRecord> entry = iterator.next();
            if (!entry.getKey().isClosed()) {
                EntityRecordNode node = EntityRecordNode.of(entry.getKey());
                entry.getValue().record(this.tick, node);
            } else {
                entry.getValue().recordClose(this.tick);
                this.closedEntityRecords.add(entry.getValue());
                iterator.remove();
            }
        }
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
        List<EntityRecord> entityRecords = new ArrayList<>(this.closedEntityRecords);
        entityRecords.addAll(this.entityRecords.values());
        return new LevelRecordPack(this.levelRecord, this.playerRecords, entityRecords);
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
            case ProtocolInfo.BLOCK_EVENT_PACKET:
            case ProtocolInfo.LEVEL_EVENT_PACKET:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V2:
            case ProtocolInfo.LEVEL_SOUND_EVENT_PACKET_V3:
            // case ProtocolInfo.LEVEL_EVENT_GENERIC_PACKET:
            case ProtocolInfo.PLAY_SOUND_PACKET:
            // case ProtocolInfo.STOP_SOUND_PACKET:
            // case ProtocolInfo.SPAWN_PARTICLE_EFFECT_PACKET:
                this.levelRecordNode.handleLevelChunkPacket(chunkIndex, packet);
                break;
        }
    }

    public void onEntitySpawn(Entity entity) {
        if (!this.recording) {
            return;
        }
        if (isEntityNeedRecord(entity)) {
            EntityRecordNode entityRecordNode = EntityRecordNode.of(entity);
            EntityRecordImpl entityRecord = new EntityRecordImpl(entity);
            entityRecord.record(this.tick, entityRecordNode);
            entityRecords.put(entity, entityRecord);
        }
    }

    public void onEntityDespawn(Entity entity) {
        if (!this.recording) {
            return;
        }

        EntityRecord record = this.entityRecords.remove(entity);
        if (record != null) {
            record.recordClose(this.tick);
            this.closedEntityRecords.add(record);
        }
    }

    public void onWeatherChange(boolean rain, int intensity) {
        if (!this.recording) {
            return;
        }
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedWeather.of(rain, intensity));
    }

    public void recordTitle(String title, String subTitle) {
        this.recordTitle(title, subTitle, 20, 20, 5);
    }

    public void recordTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedTitle.of(title, subTitle, fadeIn, stay, fadeOut));
    }

    public void recordMessage(String message) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedMessage.of(message));
    }

    public void recordActionBar(String message) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedActionBar.of(message, 1, 0, 1));
    }

    public void recordActionBar(String message, int fadeIn, int stay, int fadeOut) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedActionBar.of(message, fadeIn, fadeOut, stay));
    }

    public void recordPopup(String message) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedPopup.of(message));
    }

    public void recordCustomEvent(JsonObject obj) {
        this.levelRecordNode.offerExtraRecordUpdate(LevelUpdatedCustom.of(obj));
    }

    public void recordSoundPacket(Vector3 pos, SoundEnum soundEnum, float volume, float pitch) {
        this.recordSoundPacket(pos, soundEnum.getSound(), volume, pitch);
    }

    public void recordSoundPacket(Vector3 pos, String soundIdentifier, float volume, float pitch) {
        PlaySoundPacket pk = new PlaySoundPacket();
        pk.x = pos.getFloorX();
        pk.y = pos.getFloorY();
        pk.z = pos.getFloorZ();
        pk.name = soundIdentifier;
        pk.volume = volume;
        pk.pitch = pitch;
        this.levelRecordNode.handleLevelChunkPacket(Level.chunkHash(pk.x >> 4, pk.z >> 4), pk);
    }
}
