package net.easecation.ghosty.playback;

import cn.nukkit.entity.Entity;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackIterator;
import net.easecation.ghosty.entity.SimulatedEntity;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.entity.EntityRecordNode;
import net.easecation.ghosty.recording.entity.updated.EntityUpdated;

import java.util.List;

public class EntityPlaybackEngine {

    private final EntityRecord record;
    private final Level level;
    private final PlaybackIterator<EntityUpdated> iterator;
    private SimulatedEntity entity;

    private boolean playing = true;
    private int lastTick = -1;

    public EntityPlaybackEngine(EntityRecord record, Level level) {
        this.record = record;
        this.level = level;
        this.iterator = record.iterator();
        // 为了避免NK计时器过多，需要外部主动tick
    }

    public void onTick(int tick) {
        if (!playing) {
            return;
        }
        // int peekTick = iterator.peekTick();
        // 这边不会在录像结束时自动移除实体，而是通过EntityUpdatedClose来移除
        this.tickEntityPlayback(tick);
        this.lastTick = tick;
    }

    public void tickEntityPlayback(int tick) {
        // 往后播放
        if (tick > lastTick) {
            List<EntityUpdated> updates = iterator.pollToTick(tick);
            if (updates.isEmpty()) {
                return;
            }
            this.processEntityTick(tick, updates);
        } else if (tick < lastTick) {
            // 进行了回退，所以需要相应处理
            if (tick < iterator.getFirstTick()) {
                // 回退到了实体未创建的时候，所以移除实体
                if (this.entity != null) {
                    this.entity.close();
                }
                this.entity = null;
                GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> reset");
            } else {
                // 回退到了中间某一帧，需要重置
                List<EntityUpdated> updates = iterator.pollBackwardToTick(tick);
                if (updates.isEmpty()) {
                    return;
                }
                this.processEntityTick(tick, updates);
                GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> reset(回退)");
            }
        }
    }

    public void processEntityTick(int tick, List<EntityUpdated> updates) {
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
            GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityIdentifier() + "] " + tick + " -> spawn " + record.getNetworkId() + " item=" + entity.item);
        } else {
            // 应用updates到实体上
            for (EntityUpdated node : updates) {
                node.processTo(entity);
            }
        }
        // 因为服务端原因，被动移除实体
        if (entity != null && entity.isClosed()) {
            this.entity = null;
            GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> close(被动)");
        }
        // debug
        for (EntityUpdated node : updates) {
            if (node.getUpdateTypeId() == EntityUpdated.TYPE_POSITION_XYZ || node.getUpdateTypeId() == EntityUpdated.TYPE_ROTATION) {
                GhostyPlugin.getInstance().getLogger().debug("entity[" + record.getEntityId() + "] " + tick + " -> " + node);
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
