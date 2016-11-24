package net.easecation.ghosty.recording;

import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import net.easecation.ghosty.entity.PlaybackNPC;

import java.util.List;


/**
 * Created by boybook on 2016/11/19.
 */
public class PlaybackEngine {

    private PlayerRecord record;

    private boolean playing = true;
    protected int tick = 0;
    private boolean stopped = false;
    private PlaybackNPC npc;
    private RecordIterator iterator;

    public PlaybackEngine(PlayerRecord record) {
        this.record = record;
//        BoybookPlayerRecordTick tick0 = record.getTicks().get(0);
        iterator = record.iterator();
        RecordNode tick0 = iterator.initialValue(this.tick);
        Level level = Server.getInstance().getLevelByName(tick0.getLevel());
        if (level != null) {
            Location loc = new Location(tick0.getX(), tick0.getY(), tick0.getZ(), tick0.getY(), tick0.getPitch(), level);
            this.npc = new PlaybackNPC(loc, record.getSkin(), tick0.getTagName());
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
        this.iterator = null;
        Server.getInstance().getLogger().warning(record.getPlayer().getName() + " PlayBack stopped!");
    }

    public void onTick() {
        if (this.isPlaying()) {
            long now = iterator.peekTick();
            if(now == -1) {
                this.stopPlayback();
                return;
            }
            if(now == tick) {
                List<Updated> updatedList = iterator.peek();
                if (npc != null) updatedList.forEach((e) -> e.processTo(npc));
                iterator.pollTick();
            }
            this.tick++;
//            BoybookPlayerRecordTick tick = this.record.playBackTick(this.tick);
//            if (tick != null) {
//                Level level = Server.getInstance().getLevelByName(tick.level);
//                if (level != null) {
//                    this.npc.teleport(new Location(tick.x, tick.y, tick.z, tick.yaw, tick.pitch, level));
//                }
//                if (!tick.tagName.equals(this.npc.getNameTag())) this.npc.setNameTag(tick.tagName);
//                if (!tick.item.equals(this.npc.getInventory().getItemInHand())) this.npc.getInventory().setItemInHand(tick.item);
//            }

//            if (this.tick >= this.record.getMaxTick()) this.stopPlayback();
        }
    }

}
