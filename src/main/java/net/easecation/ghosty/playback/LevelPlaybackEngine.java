package net.easecation.ghosty.playback;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.TaskHandler;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;
import net.easecation.ghosty.recording.player.PlayerRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

public class LevelPlaybackEngine {

    private final LevelRecord record;
    private final LevelRecordNode currentNode;
    private final List<PlayerPlaybackEngine> playerPlaybackEngines = new ArrayList<>();
    private final TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    private float speed = 1;
    protected float tick = 0;
    private float lastTick = -1;
    private boolean stopped = false;
    private final Level level;
    private final PlaybackIterator<LevelUpdated> iterator;
    private final Long2ObjectMap<EntityPlaybackEngine> entityPlaybackEngines = new Long2ObjectOpenHashMap<>();

    public LevelPlaybackEngine(LevelRecord record, Level level, List<PlayerRecord> playerRecords, Collection<EntityRecord> entityRecords) {
        this.record = record;
        this.level = level;
        this.iterator = record.iterator();
        List<LevelUpdated> updates = iterator.pollToTick(this.getTick());
        this.currentNode = new LevelRecordNode();
        updates.forEach(e -> e.processTo(this.currentNode));
        this.currentNode.applyToLevel(this.getTick(), level);
        this.currentNode.clear();
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        // 玩家回放
        for (PlayerRecord playerRecord : playerRecords) {
            this.playerPlaybackEngines.add(new PlayerPlaybackEngine(playerRecord, level));
        }
        // 实体播放
        entityRecords.forEach(rec -> {
            EntityPlaybackEngine engine = new EntityPlaybackEngine(rec, level);
            this.entityPlaybackEngines.put(rec.getEntityId(), engine);
        });
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

    public List<PlayerPlaybackEngine> getPlayerPlaybackEngines() {
        return playerPlaybackEngines;
    }

    public boolean isPlaying() {
        return playing;
    }

    public int getTick() {
        return (int) tick;
    }

    public float getRealTick() {
        return this.tick;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
        // 如果是整数speed，则对tick取整
        if (this.speed == (int) this.speed) {
            tick = (int) tick;
        }
        for (PlayerPlaybackEngine playerPlaybackEngine : this.playerPlaybackEngines) {
            playerPlaybackEngine.setSpeed(speed);
        }
    }

    public void pause() {
        this.playing = false;
        for (PlayerPlaybackEngine engine : this.playerPlaybackEngines) {
            engine.pause();
        }
        for (EntityPlaybackEngine engine : this.entityPlaybackEngines.values()) {
            engine.pause();
        }
    }

    public void resume() {
        this.playing = true;
        for (PlayerPlaybackEngine engine : this.playerPlaybackEngines) {
            engine.resume();
        }
        for (EntityPlaybackEngine engine : this.entityPlaybackEngines.values()) {
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
        if (!this.playing) {
            return;
        }
        int peekTick = iterator.peekTick();
        if (peekTick == -1) {
            this.stopPlayback();
            return;
        }
        // Level事件回放
        this.tickLevelPlayback();
        // 实体回放
        this.entityPlaybackEngines.forEach((eid, engine) -> engine.onTick(this.getTick()));
        this.lastTick = this.tick;
        this.tick += this.speed;
    }

    public void tickLevelPlayback() {
        // 往后播放
        if (tick > lastTick) {
            List<LevelUpdated> updates = iterator.pollToTick(this.getTick());
            if (updates.isEmpty()) {
                return;
            }
            this.processLevelTick(this.getTick(), false, updates);
        } else if (tick < lastTick) {
            // 回退到了中间某一帧，需要重置
            List<LevelUpdated> updates = iterator.pollBackwardToTick(this.getTick());
            if (updates.isEmpty()) {
                return;
            }
            this.processLevelTick(this.getTick(), true, updates);
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug("level " + tick + " -> reset(回退)");
            }
        }
    }

    public void processLevelTick(int tick, boolean backward, List<LevelUpdated> updates) {
        // 应用updates到世界
        for (LevelUpdated node : updates) {
            if (backward) {
                node.backwardTo(this.currentNode);
            } else {
                node.processTo(this.currentNode);
            }
        }
        if (backward) {
            this.currentNode.fallbackBlockChangeTo(tick, this.level);
        }
        this.currentNode.applyToLevel(this.getTick(), this.level);
        this.currentNode.clear();
        // debug
        for (LevelUpdated node : updates) {
            if (node.getUpdateTypeId() == LevelUpdated.TYPE_LEVEL_EVENT) {
                continue;
            }
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug("level " + tick + " -> " + node);
            }
        }
    }

    public void backward(int ticks) {
        if (ticks <= 0) return;
        float tick = Math.max(0, this.tick - ticks);
        this.setTick(tick);
    }

    public void forward(int ticks) {
        if (ticks <= 0) return;
        float tick = Math.min(this.iterator.getLastTick(), this.tick + ticks);
        this.setTick(tick);
    }

    public void setTick(float tick) {
        if (tick < 0 || tick > this.iterator.getLastTick()) {
            return;
        }
        // 如果是整数speed，则对tick取整
        if (this.speed == (int) this.speed) {
            tick = (int) tick;
        }
        this.tick = tick;
        this.stopped = false;
        // 如果暂停状态，手动update一次，从而更新世界和实体
        if (!this.playing) {
            this.tickLevelPlayback();
            this.entityPlaybackEngines.forEach((eid, engine) -> {
                engine.tickEntityPlayback(this.getTick());
            });
            this.lastTick = this.tick;
        }
        for (PlayerPlaybackEngine engine : this.playerPlaybackEngines) {
            engine.setTick(this.tick);
        }
    }

}
