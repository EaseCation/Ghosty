package net.easecation.ghosty.playback;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.entity.data.LongEntityData;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.TaskHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.entity.EntityRecordNode;
import net.easecation.ghosty.recording.entity.updated.EntityUpdated;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.RecordIterator;

import java.util.ArrayList;
import java.util.List;

public class LevelPlaybackEngine {

    private final LevelRecord record;
    private final LevelRecordNode currentNode;
    private final List<PlayerPlaybackEngine> playerPlaybackEngines = new ArrayList<>();
    private final TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    protected int tick = 0;
    private boolean stopped = false;
    private final Level level;
    private final RecordIterator<LevelRecordNode, LevelUpdated> iterator;
    private final Long2ObjectMap<EntityRecord> entityRecords;
    private final Long2ObjectMap<RecordIterator<EntityRecordNode, EntityUpdated>> entityIterators;
    private final Long2ObjectMap<SimulatedEntity> simulatedEntities = new Long2ObjectOpenHashMap<>();

    public LevelPlaybackEngine(LevelRecord record, Level level, List<PlayerRecord> playerRecords, Long2ObjectMap<EntityRecord> entityRecords) {
        this.record = record;
        this.level = level;
        this.iterator = record.iterator();
        this.entityRecords = entityRecords;
        this.entityIterators = new Long2ObjectOpenHashMap<>();
        entityRecords.forEach((eid, rec) -> {
            this.entityIterators.put((long) eid, rec.iterator());
        });
        this.currentNode = iterator.initialValue(this.tick);
        this.currentNode.applyToLevel(level);
        this.currentNode.clear();
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        // 玩家回放
        for (PlayerRecord playerRecord : playerRecords) {
            this.playerPlaybackEngines.add(new PlayerPlaybackEngine(playerRecord, level));
        }
        GhostyPlugin.getInstance().getLogger().debug(level.getName() + " level playback started!");
    }

    public void setOnStopDo(Runnable onStopDo) {
        this.onStopDo = onStopDo;
    }

    public Level getLevel() {
        return level;
    }

    public LevelRecord getRecord() {
        return record;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void pause() {
        this.playing = false;
        for (PlayerPlaybackEngine engine : this.playerPlaybackEngines) {
            engine.pause();
        }
    }

    public void resume() {
        this.playing = true;
        for (PlayerPlaybackEngine engine : this.playerPlaybackEngines) {
            engine.resume();
        }
    }

    public boolean isStopped() {
        return stopped;
    }

    public void stopPlayback() {
        if (this.stopped) return;
        this.stopped = true;
        this.playing = false;
        for (PlayerPlaybackEngine playerPlaybackEngine : this.playerPlaybackEngines) {
            playerPlaybackEngine.stopPlayback();
        }
        if (this.taskHandler != null) this.taskHandler.cancel();
        if (this.onStopDo != null) this.onStopDo.run();
        GhostyPlugin.getInstance().getLogger().debug(level.getName() + " level playback stopped!");
    }

    public void onTick() {
        if (this.isPlaying()) {
            int now = iterator.peekTick();
            GhostyPlugin.getInstance().getLogger().debug("pickTick: " + now + " current: " + this.tick);
            if (now == -1) {
                this.stopPlayback();
                return;
            }
            // GhostyPlugin.getInstance().getLogger().debug("tick = " + this.tick);
            if (now == this.tick) {
                List<LevelUpdated> nodes = iterator.peek();
                nodes.forEach(e -> e.processTo(this.currentNode));
                this.currentNode.applyToLevel(this.level);
                this.currentNode.clear();
                iterator.pollTick();
                // GhostyPlugin.getInstance().getLogger().debug("playback " + this.tick + " -> " + nodes.size() + " updates");
                for (LevelUpdated node : nodes) {
                    if (node.getUpdateTypeId() == LevelUpdated.TYPE_LEVEL_EVENT) {
                        continue;
                    }
                    GhostyPlugin.getInstance().getLogger().debug("level " + this.tick + " -> " + node);
                }
            }
            // 实体回放
            this.entityIterators.forEach((eid, iterator) -> {
                int peekTick = iterator.peekTick();
                /*if (peekTick == -1) {
                    SimulatedEntity entity = this.simulatedEntities.get((long) eid);
                    if (entity != null) {
                        entity.close();
                        this.simulatedEntities.remove((long) eid);
                        GhostyPlugin.getInstance().getLogger().debug("entity[" + eid + "] " + this.tick + " -> close");
                    }
                    return;
                }*/
                if (peekTick == this.tick) {
                    List<EntityUpdated> nodes = iterator.peek();
                    SimulatedEntity entity = this.simulatedEntities.get((long) eid);
                    if (entity == null) {
                        EntityRecord rec = this.entityRecords.get((long) eid);
                        if (rec != null) {
                            EntityRecordNode init = iterator.initialValue(this.tick);
                            Location loc = new Location(init.getX(), init.getY(), init.getZ(), init.getYaw(), init.getPitch(), this.level);
                            entity = new SimulatedEntity(loc.getChunk(), Entity.getDefaultNBT(loc), rec.getNetworkId(), eid);
                            entity.setScale(init.getScale());
                            entity.setNameTag(init.getTagName());
                            entity.setScoreTag(init.getScoreTag());
                            entity.setDataProperty(new LongEntityData(Entity.DATA_FLAGS, init.getDataFlags()));
                            entity.item = init.getItem();
                            entity.spawnToAll();
                            this.simulatedEntities.put((long) eid, entity);
                            GhostyPlugin.getInstance().getLogger().debug("entity[" + eid + "] " + this.tick + " -> spawn " + rec.getNetworkId() + " item=" + entity.item);
                        } else {
                            GhostyPlugin.getInstance().getLogger().debug("entity[" + eid + "] " + this.tick + " -> EntityRecord not found");
                        }
                    }
                    for (EntityUpdated node : nodes) {
                        node.processTo(entity);
                    }
                    if (entity != null && entity.isClosed()) {
                        this.simulatedEntities.remove((long) eid);
                        GhostyPlugin.getInstance().getLogger().debug("entity[" + eid + "] " + this.tick + " -> close");
                    }
                    iterator.pollTick();
                    for (EntityUpdated node : nodes) {
                        if (node.getUpdateTypeId() == EntityUpdated.TYPE_POSITION_XYZ || node.getUpdateTypeId() == EntityUpdated.TYPE_ROTATION) {
                            GhostyPlugin.getInstance().getLogger().debug("entity[" + eid + "] " + this.tick + " -> " + node);
                        }
                    }
                }
            });
            this.tick++;
        }
    }

}
