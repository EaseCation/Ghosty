package net.easecation.ghosty;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.PlaybackEngine;
import net.easecation.ghosty.recording.PlayerRecord;
import net.easecation.ghosty.recording.RecordEngine;
import net.easecation.ghosty.runnable.PlaybackRunnable;
import net.easecation.ghosty.runnable.RecordRunnable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GhostyPlugin extends PluginBase implements Listener {

    private static GhostyPlugin instance;

    /* 正在录制中 */
    private Map<Player, RecordEngine> recorders = new HashMap<>();

    /* 重放中 */
    private List<PlaybackEngine> playbackEngines = new ArrayList<>();

    /* 录制完成的成品 */
    private List<PlayerRecord> playerRecords = new ArrayList<>();

    public static GhostyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        if (instance == null) instance = this;
        InputStream skinStream = this.getResource("skin.png");
        PlaybackNPC.defaultSkin = new Skin(skinStream);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getServer().getScheduler().scheduleRepeatingTask(new RecordRunnable(), 1);
        this.getServer().getScheduler().scheduleRepeatingTask(new PlaybackRunnable(), 1);
        //this.getServer().getScheduler().scheduleRepeatingTask(new TestRunnable(), 20);
        this.getLogger().notice("GhostyPlugin enabled!");
    }

    public Map<Player, RecordEngine> getRecorders() {
        return recorders;
    }

    public List<PlaybackEngine> getPlaybackEngines() {
        return playbackEngines;
    }

    public List<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

    public RecordEngine startNewRecord(Player player) {
        if (!this.recorders.containsKey(player)) {
            RecordEngine recorder = new RecordEngine(player);
            this.recorders.put(player, recorder);
            return recorder;
        }
        return null;
    }

    public PlaybackEngine startNewPlayBack(PlayerRecord record) {
        PlaybackEngine playBacker = new PlaybackEngine(record);
        this.playbackEngines.add(playBacker);
        return playBacker;
    }

    //@EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.startNewRecord(player);
    }
}
