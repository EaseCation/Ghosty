package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;

import java.util.List;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:08.
 */
public interface PlayerRecord {

    void record(long tick, RecordNode node);

    RecordIterator iterator();

    Player getPlayer();

    Skin getSkin();
}
