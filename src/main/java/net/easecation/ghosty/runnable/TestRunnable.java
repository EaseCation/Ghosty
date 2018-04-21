package net.easecation.ghosty.runnable;

import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.recording.PlayerRecord;

import java.util.ArrayList;

/**
 * Created by boybook on 2016/11/19.
 */
public class TestRunnable implements Runnable {

    @Override
    public void run() {
        for (PlayerRecord record: new ArrayList<>(GhostyPlugin.getInstance().getPlayerRecords())) {
            GhostyPlugin.getInstance().startNewPlayBack(record);
            GhostyPlugin.getInstance().getPlayerRecords().remove(record);
        }
    }
}
