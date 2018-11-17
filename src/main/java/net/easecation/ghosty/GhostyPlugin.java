package net.easecation.ghosty;

import cn.nukkit.entity.data.Skin;
import cn.nukkit.event.Listener;
import cn.nukkit.plugin.PluginBase;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.recording.PlayerRecord;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class GhostyPlugin extends PluginBase implements Listener {

    private static GhostyPlugin instance;

    /* 录制完成的成品 */
    private List<PlayerRecord> playerRecords = new ArrayList<>();

    public static GhostyPlugin getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        if (instance == null) instance = this;
        InputStream skinStream = this.getResource("skin.png");
        PlaybackNPC.defaultSkin = new Skin().setSkinData(skinStream);
    }

    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().notice("GhostyPlugin enabled!");
    }

    public List<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

}
