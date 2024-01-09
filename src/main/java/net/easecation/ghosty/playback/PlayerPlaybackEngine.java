package net.easecation.ghosty.playback;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import cn.nukkit.scheduler.TaskHandler;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPositionXYZ;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedRotation;

import java.util.ArrayList;
import java.util.List;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

/**
 * Created by boybook on 2016/11/19.
 */
public class PlayerPlaybackEngine {

    private final PlayerRecord record;
    private final Level level;
    private TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    private float speed = 1;
    protected float tick = 0;
    private float lastTick = -1;
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

    public PlaybackNPC getNPC() {
        return npc;
    }

    public boolean isStopped() {
        return stopped;
    }

    public int getTick() {
        return (int) tick;
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
        // this.iterator = null;
        if (this.taskHandler != null) {
            this.taskHandler.cancel();
            this.taskHandler = null;
        }
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
        this.lastTick = this.tick;
        this.tick += this.speed;
    }

    public static class InterpolationNext {
        int tickXYZ = -1;
        double x = 0;
        double y = 0;
        double z = 0;
        int tickRotation = -1;
        double yaw = 0;
        double pitch = 0;

        public void setXYZ(int tick, double x, double y, double z) {
            this.tickXYZ = tick;
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public void setRotation(int tick, double yaw, double pitch) {
            this.tickRotation = tick;
            this.yaw = yaw;
            this.pitch = pitch;
        }
    }

    public void tickPlayerPlayback() {
        // 往后播放
        if (tick > lastTick) {
            boolean realFrame = this.tick == this.getTick();
            List<PlayerUpdated> updates = iterator.pollToTick(this.getTick());
            if (realFrame && updates.isEmpty()) {
                return;
            }
            InterpolationNext interpolationNext = null;
            // 补间
            if (!realFrame) {
                InterpolationNext interpolationNext0 = new InterpolationNext();
                iterator.peekFirstMatch(u -> u.getUpdateTypeId() == PlayerUpdated.TYPE_POSITION_XYZ).ifPresent(u -> {
                    int tick = u.tick();
                    if (tick - this.getTick() > 20) {
                        // 20tick以上的补间不做
                        return;
                    }
                    PlayerUpdatedPositionXYZ xyz = (PlayerUpdatedPositionXYZ) u.entry();
                    interpolationNext0.setXYZ(tick, xyz.getX(), xyz.getY(), xyz.getZ());
                });
                iterator.peekFirstMatch(u -> u.getUpdateTypeId() == PlayerUpdated.TYPE_ROTATION).ifPresent(u -> {
                    int tick = u.tick();
                    if (tick - this.getTick() > 20) {
                        // 20tick以上的补间不做
                        return;
                    }
                    PlayerUpdatedRotation xyz = (PlayerUpdatedRotation) u.entry();
                    interpolationNext0.setRotation(tick, xyz.getYaw(), xyz.getPitch());
                });
                interpolationNext = interpolationNext0;
            }
            this.processPlayerTick(updates, interpolationNext);
        } else if (tick < lastTick) {
            // 进行了回退，所以需要相应处理
            if (tick < iterator.getFirstTick()) {
                // 回退到了实体未创建的时候，所以移除实体
                if (this.npc != null) {
                    this.npc.kill();
                }
                this.npc = null;
                iterator.pollBackwardToTick(this.getTick());
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> reset");
            } else {
                // 回退到了中间某一帧，需要重置
                List<PlayerUpdated> updates = iterator.pollBackwardToTick(this.getTick());
                if (updates.isEmpty()) {
                    return;
                }
                // 回退时，需要补充所有带有状态的Updated，因为回退时，可能会丢失某些状态
                Int2ObjectMap<PlayerUpdated> updatedByType = new Int2ObjectOpenHashMap<>();
                updates.forEach(e -> updatedByType.put(e.getUpdateTypeId(), e));
                List<PlayerUpdated> realUpdates = new ArrayList<>(updatedByType.values());
                for (int i = 0; i <= PlayerUpdated.MAX_TYPE_ID; i++) {
                    if (!updatedByType.containsKey(i)) {
                        int finalI = i;
                        iterator.peekBackwardFirstMatch(u -> u.getUpdateTypeId() == finalI)
                            .map(PlaybackIterator.RecordEntry::entry)
                            .filter(PlayerUpdated::hasStates)
                            .ifPresent(realUpdates::add);
                    }
                }
                this.processPlayerTick(realUpdates, null);
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> reset(回退)");
            }
        }
    }

    public void processPlayerTick(List<PlayerUpdated> updates, InterpolationNext interpolationNext) {
        // 需要创建实体
        if (npc == null && tick >= iterator.getFirstTick()) {
            PlayerRecordNode init = PlayerRecordNode.createZero();
            updates.forEach(e -> e.applyTo(init));
            Location loc = new Location(init.getX(), init.getY(), init.getZ(), init.getYaw(), init.getPitch(), level);
            this.npc = new PlaybackNPC(loc, record.getSkin(), init.getTagName(), null);
            this.npc.spawnToAll();
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> spawn " + record.getPlayerName());
            }
        }
        // 应用updates到实体上
        for (PlayerUpdated node : updates) {
            node.processTo(npc);
        }
        // 慢放时的插值补间
        if (interpolationNext != null && this.npc != null) {
            if (interpolationNext.tickXYZ != -1) {
                double deltaX = interpolationNext.x - npc.x;
                double deltaY = interpolationNext.y - npc.y;
                double deltaZ = interpolationNext.z - npc.z;
                double percent = (tick - this.getTick()) / (interpolationNext.tickXYZ - this.getTick());
                npc.setPosition(new Vector3(npc.x + deltaX * percent, npc.y + deltaY * percent, npc.z + deltaZ * percent));
            }
            if (interpolationNext.tickRotation != -1) {
                double rawDeltaYaw = interpolationNext.yaw - npc.yaw;
                double deltaYaw = ((rawDeltaYaw % 360) + 540) % 360 - 180; // Adjust the delta yaw to the range [-180, 180]
                double deltaPitch = interpolationNext.pitch - npc.pitch;
                double percent = (tick - this.getTick()) / (interpolationNext.tickRotation - this.getTick());
                npc.setRotation((npc.yaw + deltaYaw * percent + 360) % 360, npc.pitch + deltaPitch * percent);
            }
        }
        // 因为服务端原因，被动移除实体
        if (npc != null && npc.isClosed()) {
            this.npc = null;
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug(record.getPlayerName() + " " + tick + " -> close(被动)");
            }
        }
        // debug
        if (DEBUG_DUMP) {
            for (PlayerUpdated node : updates) {
                if (node.getUpdateTypeId() == PlayerUpdated.TYPE_POSITION_XYZ || node.getUpdateTypeId() == PlayerUpdated.TYPE_ROTATION) {
                    continue;
                }
                GhostyPlugin.getInstance().getLogger().debug("player " + tick + " -> " + node);
            }
        }
    }

    public void backward(int ticks) {
        if (ticks <= 0) return;
        float tick = Math.max(this.iterator.getFirstTick(), this.tick - ticks);
        this.setTick(tick);
    }

    public void forward(int ticks) {
        if (ticks <= 0) return;
        float tick = Math.min(this.iterator.getLastTick(), this.tick + ticks);
        this.setTick(tick);
    }

    public void setTick(float tick) {
        // 如果是整数speed，则对tick取整
        if (this.speed == (int) this.speed) {
            tick = (int) tick;
        }
        this.tick = tick;
        this.stopped = false;
        if (!this.playing) {
            this.tickPlayerPlayback();
            this.lastTick = tick;
        }
        if (this.taskHandler == null) {
            this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        }
    }

}
