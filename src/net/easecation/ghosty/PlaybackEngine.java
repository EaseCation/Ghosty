package net.easecation.ghosty;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.PlayerRecord;
import net.easecation.ghosty.recording.PlayerRecordTick;


/**
 * Created by boybook on 2016/11/19.
 */
public class PlaybackEngine {

    private PlayerRecord record;

    private boolean playing = true;
    protected int tick = 0;
    private boolean stopped = false;
    private PlaybackNPC npc;

    public PlaybackEngine(PlayerRecord record) {
        this.record = record;
        PlayerRecordTick tick0 = record.getTicks().get(0);
        Level level = Server.getInstance().getLevelByName(tick0.level);
        if (level != null) {
            Location loc = new Location(tick0.x, tick0.y, tick0.z, tick0.yaw, tick0.pitch, level);
            this.npc = new PlaybackNPC(loc, record.getSkin(), tick0.tagName);
            this.npc.spawnToAll();
            Server.getInstance().getLogger().warning(record.getPlayer().getName() + " PlayBack started!");
        } else {
            this.stopPlayback();
        }
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

    public void stopPlayback() {
        this.playing = false;
        this.stopped = true;
        if (this.npc != null) this.npc.kill();
        this.npc = null;
    }

    public void onTick() {
        if (this.isPlaying()) {
            PlayerRecordTick tick = this.record.playBackTick(this.tick);
            if (tick != null) {
                Level level = Server.getInstance().getLevelByName(tick.level);
                if (level != null) {
                    this.npc.teleport(new Location(tick.x, tick.y, tick.z, tick.yaw, tick.pitch, level));
                }
                if (!tick.tagName.equals(this.npc.getNameTag())) this.npc.setNameTag(tick.tagName);
                if (!tick.item.equals(this.npc.getInventory().getItemInHand())) this.npc.getInventory().setItemInHand(tick.item);
            }
            this.tick++;
            if (this.tick >= this.record.getMaxTick()) this.stopPlayback();
        }
    }

}
