package net.easecation.ghosty;

import java.util.List;

/**
 * RecordIterator接口定义了一个记录迭代器，用于处理游戏中玩家的行为记录。
 * 它提供了一系列方法来获取和处理记录节点（RecordNode）和更新（Updated）。
 * <p>
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:38.
 */
public interface RecordIterator<N, U> {

    /**
     * 根据给定的tick值返回RecordNode的初始值。
     * @param tick 要基于的tick值。
     * @return RecordNode的初始值。
     */
    N initialValue(int tick);

    /**
     * 返回具有与队列中第一个对象相同tick值的Updated对象列表。
     * @return Updated对象列表。
     */
    List<U> peek();

    /**
     * 返回队列中第一个对象的tick值。
     * @return 队列中第一个对象的tick值。
     */
    int peekTick();

    /**
     * 从队列中移除所有具有与队列中第一个对象相同tick值的对象。
     * 返回被移除对象的tick值。
     * @return 被移除对象的tick值。
     */
    int pollTick();
}