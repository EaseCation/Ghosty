package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.level.Level;
import cn.nukkit.level.Location;
import cn.nukkit.scheduler.TaskHandler;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.entity.PlaybackNPC;

import java.util.List;


/**
 * Created by boybook on 2016/11/19.
 */
public class PlaybackEngine {

    private final PlayerRecord record;
    private TaskHandler taskHandler;
    private Runnable onStopDo;

    private boolean playing = true;
    protected int tick = 0;
    private boolean stopped = false;
    private PlaybackNPC npc;
    private RecordIterator iterator;

    public PlaybackEngine(PlayerRecord record) {
        this(record, null, null);
    }

    public PlaybackEngine(PlayerRecord record, Level level) {
        this(record, level, null);
    }

    public PlaybackEngine(PlayerRecord record, Level level, List<Player> viewers) {
        this.record = record;
        iterator = record.iterator();
        RecordNode tick0 = iterator.initialValue(this.tick);
        if (level == null) level = Server.getInstance().getLevelByName(tick0.getLevel());
        if (level != null) {
            Location loc = new Location(tick0.getX(), tick0.getY(), tick0.getZ(), tick0.getY(), tick0.getPitch(), level);
            this.npc = new PlaybackNPC(loc, record.getSkin(), tick0.getTagName(), viewers);
            this.npc.spawnToAll();
            this.taskHandler = Server.getInstance().getScheduler().scheduleRepeatingTask(GhostyPlugin.getInstance(), this::onTick, 1);
            Server.getInstance().getLogger().debug(record.getPlayerName() + " PlayBack started!");
        } else {
            this.stopPlayback();
        }
    }

    public PlaybackEngine setOnStopDo(Runnable onStopDo) {
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

    public void stopPlayback() {
        this.playing = false;
        this.stopped = true;
        if (this.npc != null) this.npc.kill();
        this.npc = null;
        this.iterator = null;
        if (this.taskHandler != null) this.taskHandler.cancel();
        if (this.onStopDo != null) this.onStopDo.run();
        Server.getInstance().getLogger().debug(record.getPlayerName() + " PlayBack stopped!");
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
        }
    }

}
