package net.easecation.ghosty.recording;

import java.util.List;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:38.
 */
interface RecordIterator {

    RecordNode initialValue(long tick);

    List<Updated> peek();

    long peekTick();

    long pollTick();
}
