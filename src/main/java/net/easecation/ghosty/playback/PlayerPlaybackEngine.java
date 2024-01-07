package net.easecation.ghosty.playback;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;

import java.util.List;

/**
 * Created by boybook on 2016/11/19.
 */
public class PlayerPlaybackEngine {

    private final PlayerRecord record;
    private final Level level;
    private TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    protected int tick = 0;
    private int lastTick = -1;
    private boolean stopped = false;
    private PlaybackNPC npc;
    private PlaybackIterator<PlayerUpdated> iterator;

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
        this.level = level;
        this.iterator = record.iterator();
        PlayerRecordNode init = PlayerRecordNode.createZero();
        List<PlayerUpdated> updates = iterator.pollToTick(this.tick);
        updates.forEach(e -> e.applyTo(init));
        if (level == null) level = Server.getInstance().getLevelByName(init.getLevel());
        if (level != null) {
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
        if (!this.playing) {
            return;
        }
        int peekTick = iterator.peekTick();
        if (peekTick == -1) {
            this.stopPlayback();
            return;
        }
        this.tickPlayerPlayback();
        if (peekTick == tick) {
            List<PlayerUpdated> updatedList = iterator.peek();
            if (npc != null) updatedList.forEach((e) -> {
                e.processTo(npc);
                if (e.getUpdateTypeId() != PlayerUpdated.TYPE_POSITION_XYZ && e.getUpdateTypeId() != PlayerUpdated.TYPE_ROTATION) {
                    GhostyPlugin.getInstance().getLogger().debug("player " + tick + " -> " + e);
                }
            });
            iterator.pollTick();
        }
        this.lastTick = this.tick++;
    }

    public void tickPlayerPlayback() {
        // 往后播放
        if (tick > lastTick) {
            List<PlayerUpdated> updates = iterator.pollToTick(tick);
            if (updates.isEmpty()) {
                return;
            }
            this.processPlayerTick(updates);
        } else if (tick < lastTick) {
            // 进行了回退，所以需要相应处理
            if (tick < iterator.getFirstTick()) {
                // 回退到了实体未创建的时候，所以移除实体
                if (this.npc != null) {
                    this.npc.kill();
                }
                this.npc = null;
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> reset");
            } else {
                // 回退到了中间某一帧，需要重置
                List<PlayerUpdated> updates = iterator.pollBackwardToTick(tick);
                if (updates.isEmpty()) {
                    return;
                }
                this.processPlayerTick(updates);
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> reset(回退)");
            }
        }
    }

    public void processPlayerTick(List<PlayerUpdated> updates) {
        // 需要创建实体
        if (npc == null && tick >= iterator.getFirstTick()) {
            PlayerRecordNode init = PlayerRecordNode.createZero();
            updates.forEach(e -> e.applyTo(init));
            Location loc = new Location(init.getX(), init.getY(), init.getZ(), init.getYaw(), init.getPitch(), level);
            this.npc = new PlaybackNPC(loc, record.getSkin(), init.getTagName(), null);
            this.npc.spawnToAll();
            GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> spawn " + record.getPlayerName());
        }
        // 应用updates到实体上
        for (PlayerUpdated node : updates) {
            node.processTo(npc);
        }
        // 因为服务端原因，被动移除实体
        if (npc != null && npc.isClosed()) {
            this.npc = null;
            GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> close(被动)");
        }
        // debug
        for (PlayerUpdated node : updates) {
            if (node.getUpdateTypeId() == PlayerUpdated.TYPE_POSITION_XYZ || node.getUpdateTypeId() == PlayerUpdated.TYPE_ROTATION) {
                continue;
            }
            GhostyPlugin.getInstance().getLogger().debug("player " + tick + " -> " + node);
        }
    }

    public void backward(int ticks) {
        if (ticks <= 0) return;
        this.tick -= ticks;
        if (this.tick < 0) this.tick = 0;
        // 如果暂停状态，手动update一次，从而更新世界和实体
        if (!this.playing) {
            this.tickPlayerPlayback();
            this.lastTick = this.tick;
        }
    }

    public void forward(int ticks) {
        if (ticks <= 0) return;
        this.tick += ticks;
        if (!this.playing) {
            this.tickPlayerPlayback();
            this.lastTick = this.tick;
        }
    }

}
