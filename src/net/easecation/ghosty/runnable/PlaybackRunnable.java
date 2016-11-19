package net.easecation.ghosty.runnable;

import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.PlaybackEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boybook on 2016/11/19.
 */
public class PlaybackRunnable implements Runnable {

    @Override
    public void run() {
        List<PlaybackEngine> stopped = new ArrayList<>();
        for (PlaybackEngine playBacker: GhostyPlugin.getInstance().getPlaybackEngines()) {
            playBacker.onTick();
            if (playBacker.isStopped()) stopped.add(playBacker);
        }
        for (PlaybackEngine s: stopped) {
            GhostyPlugin.getInstance().getPlaybackEngines().remove(s);
        }
    }
}
