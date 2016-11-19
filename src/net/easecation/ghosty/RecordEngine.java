package net.easecation.ghosty;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import net.easecation.ghosty.recording.PlayerRecord;
import net.easecation.ghosty.recording.PlayerRecordTick;

public class RecordEngine {

    private Player player;

    protected int tick = 0;
    private boolean recording = true;
    private boolean stopped = false;

    protected PlayerRecord record;

    public RecordEngine(Player player) {
        this.player = player;
        this.record = new PlayerRecord(Server.getInstance().getOfflinePlayer(player.getName()), player.getSkin());
        Server.getInstance().getLogger().warning(player.getName() + " Record started!");
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

    public void onTick() {
        if (this.isRecording()) {
            if (!this.player.isOnline()) {
                GhostyPlugin.getInstance().getPlayerRecords().add(this.stopRecord());
            }
            PlayerRecordTick tick = new PlayerRecordTick(this.getPlayer().getX(), this.getPlayer().getY(), this.getPlayer().getZ(), this.getPlayer().getYaw(), this.getPlayer().getPitch(), this.getPlayer().getLevel().getFolderName(), this.player.getNameTag(), this.player.getInventory() == null ? Item.get(0) : this.player.getInventory().getItemInHand());
            this.record.recordTick(this.tick, tick);
        }
        this.tick++;
    }

    public PlayerRecord stopRecord() {
        this.setRecording(false);
        this.stopped = true;
        Server.getInstance().getLogger().warning(this.player.getName() + " Record stopped!");
        this.record.setMaxTick(this.tick);
        this.record.setStopTime(System.currentTimeMillis());
        return this.record;
    }

}
