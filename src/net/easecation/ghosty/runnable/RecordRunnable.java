package net.easecation.ghosty.runnable;

import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.RecordEngine;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by boybook on 2016/11/19.
 */
public class RecordRunnable implements Runnable {

    @Override
    public void run() {
        List<RecordEngine> stopped = new ArrayList<>();
        for (RecordEngine recorder: GhostyPlugin.getInstance().getRecorders().values()) {
            recorder.onTick();
            if (recorder.isStopped()) stopped.add(recorder);
        }
        for (RecordEngine s: stopped) {
            GhostyPlugin.getInstance().getRecorders().remove(s.getPlayer());
        }
    }
}
