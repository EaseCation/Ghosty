package net.easecation.ghosty;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.EventHandler;
import cn.nukkit.event.Listener;
import cn.nukkit.event.player.PlayerJoinEvent;
import cn.nukkit.plugin.PluginBase;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.PlayerRecord;
import net.easecation.ghosty.runnable.PlaybackRunnable;
import net.easecation.ghosty.runnable.RecordRunnable;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GhostyPlugin extends PluginBase implements Listener {

    private static GhostyPlugin instance;

    private Map<Player, RecordEngine> recorders = new HashMap<>();
    private List<PlaybackEngine> playbackEngines = new ArrayList<>();

    private List<PlayerRecord> playerRecords = new ArrayList<>();

    private Map<String, PlayerRecord> records = new HashMap<>();

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

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        this.startNewRecord(player);
    }
}
