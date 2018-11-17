package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.item.Item;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;

public class RecordEngine {

    private final Player player;
    private final TaskHandler taskHandler;

    private boolean unifySave = true;
    private int tick = 0;
    private boolean recording = true;
    private boolean stopped = false;

    private PlayerRecord record;

    public RecordEngine(Player player) {
        this.player = player;
        this.record = new LmlPlayerRecord(player);
        this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
        Server.getInstance().getLogger().warning(player.getName() + " Record started!");
    }

    /**
     * 统一保存到本插件内存中
     * @param unifySave 是否统一保存
     * @return 本对象
     */
    public RecordEngine setUnifySave(boolean unifySave) {
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

    public void onTick() {
        if (this.isRecording()) {
            if (!this.player.isOnline()) {
                stopRecord();
            }
            this.record.record(this.tick, RecordNode.of(this.player));
        }
        this.tick++;
    }

    public PlayerRecord stopRecord() {
        this.setRecording(false);
        this.stopped = true;
        Server.getInstance().getLogger().warning(this.player.getName() + " Record stopped!");
        this.taskHandler.cancel();
        if (unifySave) GhostyPlugin.getInstance().getPlayerRecords().add(this.record);
        return this.record;
    }

}
