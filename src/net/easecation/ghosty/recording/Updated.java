package net.easecation.ghosty.recording;

import net.easecation.ghosty.entity.PlaybackNPC;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:20.
 */
public interface Updated {

    void processTo(PlaybackNPC ghost);

    RecordNode applyTo(RecordNode node);
}
