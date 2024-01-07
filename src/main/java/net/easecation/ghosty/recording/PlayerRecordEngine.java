package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.DataPacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.network.protocol.TakeItemEntityPacket;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.recording.player.LmlPlayerRecord;
import net.easecation.ghosty.recording.player.PlayerRecord;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.recording.player.updated.PlayerUpdated;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedAnimate;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedEntityEvent;
import net.easecation.ghosty.recording.player.updated.PlayerUpdatedTakeItemEntity;
import org.itxtech.synapseapi.SynapsePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * 录制引擎
 * player -> RecordNode -> 判断与上一状态的异同 -> PlayerUpdate -> 都存储在了PlayerRecord中（内存） -> 需要保存的时候，再二进制化保存
 */
public class PlayerRecordEngine {

    private final Player player;
    private final TaskHandler taskHandler;

    private boolean unifySave = true;
    private int tick = 0;
    private boolean recording = true;
    private boolean stopped = false;

    /**
     * 本次录制的记录
     */
    private final PlayerRecord record;

    public PlayerRecordEngine(Player player) {
        this(player, LmlPlayerRecord::new);
    }

    public PlayerRecordEngine(Player player, Function<Player, PlayerRecord> recordFactory) {
        this.player = player;
        this.record = recordFactory.apply(player);
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        GhostyPlugin.getInstance().recordingPlayerEngines.put(player, this);
        GhostyPlugin.getInstance().getLogger().debug(player.getName() + " record started!");
    }

    public void setTick(int tick) {
        this.tick = tick;
    }

    /**
     * 统一保存到本插件内存中
     * @param unifySave 是否统一保存
     * @return 本对象
     */
    public PlayerRecordEngine setUnifySave(boolean unifySave) {
        this.unifySave = unifySave;
        return this;
    }

    public boolean isRecording() {
        return recording;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setRecording(boolean recording) {
        this.recording = recording;
    }

    public Player getPlayer() {
        return player;
    }

    private final List<PlayerUpdated> extraUpdates = new ArrayList<>();

    public void onTick() {
        if (this.isRecording()) {
            if (!this.player.isOnline()) {
                stopRecord();
            }
            PlayerRecordNode playerRecordNode = PlayerRecordNode.of(this.player);
            playerRecordNode.offerExtraUpdate(extraUpdates);
            this.record.record(this.tick, playerRecordNode);
            extraUpdates.clear();
        }
        this.tick++;
    }

    public void onPacketSendEvent(DataPacket packet) {
        if (packet instanceof AnimatePacket pk) {
            if (pk.eid == this.player.getId() || pk.eid == SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID) {
                this.extraUpdates.add(PlayerUpdatedAnimate.of(pk.action.getId(), pk.rowingTime));
            }
        } else if (packet instanceof EntityEventPacket pk) {
            if (pk.eid == this.player.getId() || pk.eid == SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID) {
                this.extraUpdates.add(PlayerUpdatedEntityEvent.of(pk.event, pk.data));
            }
        } else if (packet instanceof TakeItemEntityPacket pk) {
            if (pk.entityId == this.player.getId() || pk.entityId == SynapsePlayer.SYNAPSE_PLAYER_ENTITY_ID) {
                this.extraUpdates.add(PlayerUpdatedTakeItemEntity.of(pk.target));
            }
        }
    }

    public void onPacketReceiveEvent(DataPacket packet) {
        if (packet instanceof AnimatePacket pk) {
            this.extraUpdates.add(PlayerUpdatedAnimate.of(pk.action.getId(), pk.rowingTime));
        }
    }

    public PlayerRecord stopRecord() {
        this.setRecording(false);
        this.stopped = true;
        GhostyPlugin.getInstance().getLogger().debug(this.player.getName() + " record stopped!");
        this.taskHandler.cancel();
        if (unifySave) GhostyPlugin.getInstance().getPlayerRecords().add(this.record);
        return this.record;
    }

    public PlayerRecord getRecord() {
        return record;
    }
}
