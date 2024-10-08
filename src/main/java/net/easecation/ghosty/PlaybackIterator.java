package net.easecation.ghosty;

import java.util.*;
import java.util.function.Predicate;

public class PlaybackIterator<T> {

    public record RecordEntry<T>(int tick, T entry) {}

    List<RecordEntry<T>> list = new ArrayList<>();
    int currentIndex = 0; // 当前迭代的位置

    public int getCurrentIndex() {
        return currentIndex;
    }

    /**
     * 插入元素时保持顺序
     */
    public void insert(int tick, T entry) {
        // 如果列表为空或最后一个元素的tick小于新元素的tick，则直接添加
        if (list.isEmpty() || list.get(list.size() - 1).tick() <= tick) {
            list.add(new RecordEntry<>(tick, entry));
            return;
        }
        // 否则，找到正确的插入位置
        ListIterator<RecordEntry<T>> it = list.listIterator();
        while (it.hasNext()) {
            if (it.next().tick() > tick) {
                it.previous();
                it.add(new RecordEntry<>(tick, entry));
                return;
            }
        }
    }

    public int getFirstTick() {
        if (list.isEmpty()) return -1;
        return list.get(0).tick();
    }

    public int getLastTick() {
        if (list.isEmpty()) return -1;
        return list.get(list.size() - 1).tick();
    }

    public List<T> pollToTick(int tick) {
        if (this.currentIndex >= list.size()) return Collections.emptyList();  // 如果当前迭代器已经到达末尾，直接返回空列表
        List<T> ans = new LinkedList<>();
        while (currentIndex < list.size() && list.get(currentIndex).tick() <= tick) {
            ans.add(list.get(currentIndex).entry());
            currentIndex++;
        }
        return ans;
    }

    public List<T> pollBackwardToTick(int tick) {
        if (this.currentIndex <= 0) return Collections.emptyList();  // 如果当前迭代器已经到达开头，直接返回空列表
        List<T> ans = new LinkedList<>();
        while (currentIndex > 0 && list.get(currentIndex - 1).tick() >= tick) {
            currentIndex--;
            ans.add(list.get(currentIndex).entry());
        }
        // 如果当前元素的tick值小于目标tick值，将currentIndex向前移动一位
        if (currentIndex < list.size() && list.get(currentIndex).tick() < tick) {
            currentIndex++;
        }
        return ans;
    }

    /**
     * 返回当前迭代器位置的所有记录条目，这些条目的tick值与当前位置的tick值相同。
     * 如果当前迭代器位置已经超过列表的末尾，将返回一个空列表。
     *
     * @return 一个包含当前迭代器位置的所有记录条目的列表
     */
    public List<T> peek() {
        List<T> ans = new LinkedList<>();
        if (currentIndex >= list.size()) return ans;

        RecordEntry<T> current = list.get(currentIndex);
        ans.add(current.entry());
        int nextIndex = currentIndex + 1;

        while (nextIndex < list.size() && list.get(nextIndex).tick() == current.tick()) {
            ans.add(list.get(nextIndex).entry());
            nextIndex++;
        }

        return ans;
    }

    /**
     * 返回满足给定谓词的第一个记录条目，从当前迭代器位置开始向后查找。
     * 如果没有找到满足谓词的记录条目，将返回一个空的Optional。
     *
     * @param predicate 用于测试记录条目的谓词
     * @return 一个Optional，可能包含满足谓词的第一个记录条目
     */
    public Optional<RecordEntry<T>> peekFirstMatch(Predicate<T> predicate) {
        if (currentIndex >= list.size()) return Optional.empty();

        int nextIndex = currentIndex;
        while (nextIndex < list.size()) {
            if (predicate.test(list.get(nextIndex).entry())) {
                return Optional.of(list.get(nextIndex));
            }
            nextIndex++;
        }

        return Optional.empty();
    }

    /**
     * 返回满足给定谓词的第一个记录条目，从当前迭代器位置开始向前查找。
     * 如果没有找到满足谓词的记录条目，将返回一个空的Optional。
     *
     * @param predicate 用于测试记录条目的谓词
     * @return 一个Optional，可能包含满足谓词的第一个记录条目
     */
    public Optional<RecordEntry<T>> peekBackwardFirstMatch(Predicate<T> predicate) {
        if (currentIndex <= 0) return Optional.empty();

        int prevIndex = currentIndex - 1;
        while (prevIndex >= 0) {
            if (predicate.test(list.get(prevIndex).entry())) {
                return Optional.of(list.get(prevIndex));
            }
            prevIndex--;
        }

        return Optional.empty();
    }

    /**
     * 返回当前迭代器位置的tick值。
     * 如果当前迭代器位置已经超过列表的末尾，将返回-1。
     *
     * @return 当前迭代器位置的tick值，或-1如果迭代器已经超过列表的末尾
     */
    public int peekTick() {
        if (currentIndex >= list.size()) return -1;
        return list.get(currentIndex).tick();
    }


    public int pollTick() {
        if (currentIndex >= list.size()) return -1;
        int tick = list.get(currentIndex).tick();
        currentIndex++; // 更新迭代器位置
        while (currentIndex < list.size() && list.get(currentIndex).tick() == tick) {
            currentIndex++;
        }
        return tick;
    }

    /**
     * 返回当前迭代器位置前一个位置的所有记录条目，这些条目的tick值与前一个位置的tick值相同。
     * 如果当前迭代器位置已经在列表的开头，将返回一个空列表。
     *
     * @return 一个包含当前迭代器位置前一个位置的所有记录条目的列表
     */
    public List<T> peekBackward() {
        List<T> ans = new LinkedList<>();
        if (currentIndex <= 0) return ans;

        int prevIndex = currentIndex - 1;
        RecordEntry<T> current = list.get(prevIndex);
        ans.add(current.entry());

        while (prevIndex > 0 && list.get(prevIndex - 1).tick() == current.tick()) {
            prevIndex--;
            ans.add(list.get(prevIndex).entry());
        }

        return ans;
    }

    /**
     * 返回当前迭代器位置前一个位置的tick值。
     * 如果当前迭代器位置已经在列表的开头，将返回-1。
     *
     * @return 当前迭代器位置前一个位置的tick值，或-1如果迭代器已经在列表的开头
     */
    public int peekTickBackward() {
        if (currentIndex <= 0) return -1;
        return list.get(currentIndex - 1).tick();
    }

    public int pollTickBackward() {
        if (currentIndex <= 0) return -1;
        currentIndex--; // 向后移动迭代器位置
        int tick = list.get(currentIndex).tick();
        while (currentIndex > 0 && list.get(currentIndex - 1).tick() == tick) {
            currentIndex--;
        }
        return tick;
    }

    /**
     * 独立测试
     */
    public static void main(String[] args) {
        // 生成随机测试数据
        long insertStart = System.nanoTime();
        PlaybackIterator<Integer> iterator = new PlaybackIterator<>();
        for (int i = 0; i < 1000; i++) {
            for (int i1 = 0; i1 < 5; i1++) {
                iterator.insert(i, i);
            }
        }
        System.out.println("size: " + iterator.list.size());
        long insertEnd = System.nanoTime();
        System.out.println("插入耗时：" + (insertEnd - insertStart) / 1000000 + "ms");

        long start = System.nanoTime();

        // 测试peek和pollTick方法
        System.out.println("===== 测试 peek 和 pollTick 方法 =====");
        while (iterator.peekTick() != -1) {
            int tick = iterator.peekTick();
            List<Integer> entries = iterator.peek();
            //System.out.println("tick=" + tick);
            //System.out.println(entries);
            int poll = iterator.pollTick();
            assert poll == tick;
        }
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成 peek 和 pollTick 方法 =====");

        // 测试peekBackward和pollTickBackward方法
        System.out.println("===== 测试 peekBackward和pollTickBackward 方法 =====");
        while (iterator.peekTickBackward() != -1) {
            int tick = iterator.peekTickBackward();
            List<Integer> entries = iterator.peekBackward();
            //System.out.println("tick=" + tick);
            //System.out.println(entries);
            int poll = iterator.pollTickBackward();
            assert poll == tick;
        }
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成 peekBackward和pollTickBackward 方法 =====");

        // 测试pollToTick(50)方法
        System.out.println("===== 测试 pollToTick(50) 方法 =====");
        List<Integer> result = iterator.pollToTick(50);
        for (Integer i : result) {
            assert i <= 50;
        }
        System.out.println(result);
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成 pollToTick(50) 方法 =====");

        // 测试反向pollToTick(25)方法
        System.out.println("===== 测试反向 pollToTick(25) 方法 =====");
        result = iterator.pollToTick(25);
        assert result.isEmpty();
        System.out.println(result);
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成反向pollToTick(25)方法 =====");

        // 测试pollBackwardToTick方法
        System.out.println("===== 测试 pollBackwardToTick(25) 方法 =====");
        result = iterator.pollBackwardToTick(25);
        for (Integer i : result) {
            assert i >= 25;
        }
        System.out.println(result);
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成pollBackwardToTick(25)方法 =====");

        // 测试反向pollBackwardToTick(50)方法
        System.out.println("===== 测试反向 pollBackwardToTick(50) 方法 =====");
        result = iterator.pollBackwardToTick(50);
        assert result.isEmpty();
        System.out.println(result);
        System.out.println("当前指针在" + iterator.getCurrentIndex() + "处");
        System.out.println("peekTick = " + iterator.peekTick());
        System.out.println("peekTickBackward = " + iterator.peekTickBackward());
        System.out.println("===== 完成反向pollBackwardToTick(50)方法 =====");

        long end = System.nanoTime();
        System.out.println("耗时：" + (end - start) / 1000000 + "ms");
    }

}
