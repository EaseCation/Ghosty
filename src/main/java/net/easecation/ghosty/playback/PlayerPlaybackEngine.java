package net.easecation.ghosty.playback;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.RecordIterator;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;

import java.util.List;


/**
 * Created by boybook on 2016/11/19.
 */
public class PlayerPlaybackEngine {

    private final PlayerRecord record;
    private TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    protected int tick = 0;
    private boolean stopped = false;
    private PlaybackNPC npc;
    private RecordIterator<PlayerRecordNode, PlayerUpdated> iterator;

    public PlayerPlaybackEngine(PlayerRecord record) {
        this(record, null, null);
    }

    public PlayerPlaybackEngine(PlayerRecord record, Level level) {
        this(record, level, null);
    }

    public PlayerPlaybackEngine(PlayerRecord record, Level level, List<Player> viewers) {
        this(record, level, viewers, null);
    }

    public PlayerPlaybackEngine(PlayerRecord record, Level level, List<Player> viewers, Skin skin) {
        this.record = record;
        iterator = record.iterator();
        PlayerRecordNode tick0 = iterator.initialValue(this.tick);
        if (level == null) level = Server.getInstance().getLevelByName(tick0.getLevel());
        if (level != null) {
            Location loc = new Location(tick0.getX(), tick0.getY(), tick0.getZ(), tick0.getY(), tick0.getPitch(), level);
            this.npc = new PlaybackNPC(loc, skin != null ? skin : record.getSkin(), tick0.getTagName(), viewers);
            this.npc.spawnToAll();
            this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
            GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " playBack started!");
        } else {
            this.stopPlayback();
        }
    }

    public PlayerPlaybackEngine setOnStopDo(Runnable onStopDo) {
        this.onStopDo = onStopDo;
        return this;
    }

    public PlayerRecord getRecord() {
        return record;
    }

    public boolean isStopped() {
        return stopped;
    }

    public boolean isPlaying() {
        return playing;
    }

    public void pause() {
        this.playing = false;
    }

    public void resume() {
        this.playing = true;
    }

    public void stopPlayback() {
        this.playing = false;
        this.stopped = true;
        if (this.npc != null) this.npc.kill();
        this.npc = null;
        this.iterator = null;
        if (this.taskHandler != null) this.taskHandler.cancel();
        if (this.onStopDo != null) this.onStopDo.run();
        GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " playBack stopped!");
    }

    public void onTick() {
        if (this.isPlaying()) {
            long now = iterator.peekTick();
            if (now == -1) {
                this.stopPlayback();
                return;
            }
            if (now == tick) {
                List<PlayerUpdated> updatedList = iterator.peek();
                if (npc != null) updatedList.forEach((e) -> {
                    e.processTo(npc);
                    if (e.getUpdateTypeId() != PlayerUpdated.TYPE_POSITION_XYZ && e.getUpdateTypeId() != PlayerUpdated.TYPE_ROTATION) {
                        GhostyPlugin.getInstance().getLogger().debug("player " + tick + " -> " + e);
                    }
                });
                iterator.pollTick();
            }
            this.tick++;
        }
    }

}
