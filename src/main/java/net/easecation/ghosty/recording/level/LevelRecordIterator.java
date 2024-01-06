package net.easecation.ghosty.recording.level;

import net.easecation.ghosty.recording.level.updated.LevelUpdated;
import net.easecation.ghosty.recording.player.RecordIterator;

import java.util.*;

public class LevelRecordIterator implements RecordIterator<LevelRecordNode, LevelUpdated> {

    /**
     * 一个静态比较器，用于根据RecordPair对象的tick值进行排序。
     */
    static Comparator<LevelRecordImpl.RecordPair> comparator = (recordPairA, recordPairB) -> {
        if (recordPairA.tick < recordPairB.tick) {
            return -1;
        } else if (recordPairA.tick == recordPairB.tick) {
            return 0;
        }
        return 1;
    };

    /**
     * 一个优先队列，用于存储和管理RecordPair对象。
     * 优先队列根据RecordPair对象的tick值进行排序。
     */
    PriorityQueue<LevelRecordImpl.RecordPair> queue = new PriorityQueue<>(comparator);

    /**
     * 根据给定的tick值返回RecordNode的初始值。
     * 它将所有具有相同tick值的更新应用于RecordNode。
     * @param tick 要基于的tick值。
     * @return RecordNode的初始值。
     */
    @Override
    public LevelRecordNode initialValue(int tick) {
        LevelRecordNode n = new LevelRecordNode();
        if (queue.peek() == null) return n;
        while (!queue.isEmpty() && queue.peek().tick < tick) {
            queue.poll();
        }
        if (queue.peek() == null) {
            return n;
        }
        while (!queue.isEmpty() && queue.peek().tick == tick) {
            LevelUpdated updated = queue.poll().updated;
            updated.processTo(n);
        }
        return n;
    }

    /**
     * 返回具有与队列中第一个对象相同tick值的Updated对象列表。
     * @return Updated对象列表。
     */
    @Override
    public List<LevelUpdated> peek() {
        List<LevelUpdated> ans = new LinkedList<>();
        if(queue.isEmpty()) return ans;
        int tick = queue.peek().tick;
        PriorityQueue<LevelRecordImpl.RecordPair> tempQueue = new PriorityQueue<>(queue);
        while (!tempQueue.isEmpty() && tempQueue.peek().tick == tick) {
            ans.add(tempQueue.poll().updated);
        }
        return ans;
    }

    /**
     * 返回队列中第一个对象的tick值。
     * @return 队列中第一个对象的tick值。
     */
    @Override
    public int peekTick() {
        if (queue.isEmpty()) return -1;
        return queue.peek().tick;
    }

    /**
     * 从队列中移除所有具有与队列中第一个对象相同tick值的对象。
     * 返回被移除对象的tick值。
     * @return 被移除对象的tick值。
     */
    @Override
    public int pollTick() {
        if (queue.isEmpty()) return -1;
        int tick = queue.peek().tick;
        while (!queue.isEmpty() && queue.peek().tick == tick) {
            queue.poll();
        }
        return tick;
    }
}
