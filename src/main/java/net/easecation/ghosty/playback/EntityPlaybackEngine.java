package net.easecation.ghosty.playback;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.math.Vector3;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.entity.EntityRecordNode;
import net.easecation.ghosty.recording.entity.updated.EntityUpdated;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedPositionXYZ;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedRotation;

import java.util.ArrayList;
import java.util.List;

import static net.easecation.ghosty.GhostyPlugin.DEBUG_DUMP;

public class EntityPlaybackEngine {

    private final EntityRecord record;
    private final Level level;
    private final PlaybackIterator<EntityUpdated> iterator;
    private SimulatedEntity entity;

    private boolean playing = true;
    private float lastTick = -1;

    public EntityPlaybackEngine(EntityRecord record, Level level) {
        this.record = record;
        this.level = level;
        this.iterator = record.iterator();
        // 为了避免NK计时器过多，需要外部主动tick
    }

    public void onTick(float tick) {
        if (!playing) {
            return;
        }
        // int peekTick = iterator.peekTick();
        // 这边不会在录像结束时自动移除实体，而是通过EntityUpdatedClose来移除
        this.tickEntityPlayback(tick);
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

    public void tickEntityPlayback(float tick) {
        // 往后播放
        if (tick > lastTick) {
            boolean realFrame = tick == (int) tick;
            int intTick = (int) tick;
            List<EntityUpdated> updates = iterator.pollToTick((int) tick);
            if (realFrame && updates.isEmpty()) {
                return;
            }
            InterpolationNext interpolationNext = null;
            // 补间
            if (!realFrame) {
                InterpolationNext interpolationNext0 = new InterpolationNext();
                iterator.peekFirstMatch(u -> u.getUpdateTypeId() == PlayerUpdated.TYPE_POSITION_XYZ).ifPresent(u -> {
                    int tick0 = u.tick();
                    if (tick0 - intTick > 20) {
                        // 20tick以上的补间不做
                        return;
                    }
                    PlayerUpdatedPositionXYZ xyz = (PlayerUpdatedPositionXYZ) u.entry();
                    interpolationNext0.setXYZ(tick0, xyz.getX(), xyz.getY(), xyz.getZ());
                });
                iterator.peekFirstMatch(u -> u.getUpdateTypeId() == PlayerUpdated.TYPE_ROTATION).ifPresent(u -> {
                    int tick0 = u.tick();
                    if (tick0 - intTick > 20) {
                        // 20tick以上的补间不做
                        return;
                    }
                    PlayerUpdatedRotation xyz = (PlayerUpdatedRotation) u.entry();
                    interpolationNext0.setRotation(tick0, xyz.getYaw(), xyz.getPitch());
                });
                interpolationNext = interpolationNext0;
            }
            this.processEntityTick(tick, updates, interpolationNext);
        } else if (tick < lastTick) {
            // 进行了回退，所以需要相应处理
            if (tick < iterator.getFirstTick()) {
                // 回退到了实体未创建的时候，所以移除实体
                if (this.entity != null) {
                    this.entity.close();
                }
                this.entity = null;
                iterator.pollBackwardToTick((int) tick);
                if (DEBUG_DUMP) {
                    GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> reset");GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> reset");
                }
            } else {
                // 回退到了中间某一帧，需要重置
                List<EntityUpdated> updates = iterator.pollBackwardToTick((int) tick);
                if (updates.isEmpty()) {
                    return;
                }
                // 回退时，需要补充所有带有状态的Updated，因为回退时，可能会丢失某些状态
                Int2ObjectMap<EntityUpdated> updatedByType = new Int2ObjectOpenHashMap<>();
                updates.forEach(e -> updatedByType.put(e.getUpdateTypeId(), e));
                List<EntityUpdated> realUpdates = new ArrayList<>(updatedByType.values());
                for (int i = 0; i <= EntityUpdated.MAX_TYPE_ID; i++) {
                    if (!updatedByType.containsKey(i)) {
                        int finalI = i;
                        iterator.peekBackwardFirstMatch(u -> u.getUpdateTypeId() == finalI)
                            .map(PlaybackIterator.RecordEntry::entry)
                            .filter(EntityUpdated::hasStates)
                            .ifPresent(realUpdates::add);
                    }
                }
                this.processEntityTick(tick, realUpdates, null);
                if (DEBUG_DUMP) {
                    GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> reset(回退)");
                }
            }
        }
        this.lastTick = tick;
    }

    public void processEntityTick(float tick, List<EntityUpdated> updates, InterpolationNext interpolationNext) {
        int intTick = (int) tick;
        // 需要创建实体
        if (entity == null && tick >= iterator.getFirstTick()) {
            EntityRecordNode init = EntityRecordNode.createZero();
            updates.forEach(e -> e.applyTo(init));
            Location loc = new Location(init.getX(), init.getY(), init.getZ(), init.getYaw(), init.getPitch(), this.level);
            entity = new SimulatedEntity(loc.getChunk(), Entity.getDefaultNBT(loc), record.getNetworkId(), record.getEntityIdentifier(), record.getEntityId());
            // 生成前就需要应用了
            for (EntityUpdated node : updates) {
                node.processTo(entity);
            }
            entity.spawnToAll();
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityIdentifier() + "] " + tick + " -> spawn " + record.getNetworkId() + " item=" + entity.item);
            }
        } else {
            // 应用updates到实体上
            for (EntityUpdated node : updates) {
                node.processTo(entity);
            }
        }
        // 慢放时的插值补间
        if (interpolationNext != null && entity != null) {
            if (interpolationNext.tickXYZ != -1) {
                double deltaX = interpolationNext.x - entity.x;
                double deltaY = interpolationNext.y - entity.y;
                double deltaZ = interpolationNext.z - entity.z;
                double percent = (tick - intTick) / (interpolationNext.tickXYZ - intTick);
                entity.setPosition(new Vector3(entity.x + deltaX * percent, entity.y + deltaY * percent, entity.z + deltaZ * percent));
            }
            if (interpolationNext.tickRotation != -1) {
                double rawDeltaYaw = interpolationNext.yaw - entity.yaw;
                double deltaYaw = ((rawDeltaYaw % 360) + 540) % 360 - 180; // Adjust the delta yaw to the range [-180, 180]
                double deltaPitch = interpolationNext.pitch - entity.pitch;
                double percent = (tick - intTick) / (interpolationNext.tickRotation - intTick);
                entity.setRotation((entity.yaw + deltaYaw * percent + 360) % 360, entity.pitch + deltaPitch * percent);
            }
        }
        // 因为服务端原因，被动移除实体
        if (entity != null && entity.isClosed()) {
            this.entity = null;
            if (DEBUG_DUMP) {
                GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> close(被动)");
            }
        }
        // debug
        if (DEBUG_DUMP) {
            for (EntityUpdated node : updates) {
                if (node.getUpdateTypeId() == EntityUpdated.TYPE_POSITION_XYZ || node.getUpdateTypeId() == EntityUpdated.TYPE_ROTATION) {
                    GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> " + node);
                }
            }
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
}
