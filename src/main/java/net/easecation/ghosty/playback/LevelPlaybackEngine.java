package net.easecation.ghosty.playback;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.recording.level.updated.LevelUpdated;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.RecordIterator;

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

    public LevelPlaybackEngine(LevelRecord record, Level level, List<PlayerRecord> playerRecords) {
        this.record = record;
        this.level = level;
        this.iterator = record.iterator();
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
            this.tick++;
        }
    }

}
