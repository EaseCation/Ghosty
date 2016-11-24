package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;

import java.util.*;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
class LmlPlayerRecord implements PlayerRecord {

    private RecordNode last = RecordNode.ZERO;

    private List<RecordPair> rec = new LinkedList<>();

    private Player player;

    LmlPlayerRecord(Player player) {
        this.player = player;
    }

    @Override
    public void record(long tick, RecordNode node) {
        double lx = last.getX(), x = node.getX();
        double ly = last.getY(), y = node.getY();
        double lz = last.getZ(), z = node.getZ();
        if (lx != x || ly != y || lz != z)
            push(tick, UpdatedPositionXYZ.of(x, y, z));
        double la = last.getYaw(), a = node.getYaw();
        double lp = last.getPitch(), p = node.getPitch();
        if(la != a || lp != p)
            push(tick, UpdatedRotation.of(a, p));
        String ln = last.getTagName(), n = node.getTagName();
        if(!Objects.equals(ln, n))
            push(tick, UpdatedTagName.of(n));
        String lw = last.getLevel(), w = node.getLevel();
        if(!Objects.equals(lw, w))
            push(tick, UpdatedWorld.of(w));
        Item li = last.getItem(), i = node.getItem();
        if(!Objects.equals(li, i))
            push(tick, UpdatedItem.of(i));
    }

    private void push(long tick, Updated updated) {
        rec.add(new RecordPair(tick, updated));
    }

    private class RecordPair{
        private RecordPair(long tick, Updated updated) {
            this.tick = tick;
            this.updated = updated;
        }
        long tick; Updated updated;
    }

    @Override
    public RecordIterator iterator() {
        LmlRecordIterator recordIterator = new LmlRecordIterator();
        rec.forEach((e) -> recordIterator.queue.offer(e));
        return recordIterator;
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public Skin getSkin() {
        return player.getSkin();
    }

    private static class LmlRecordIterator implements RecordIterator {

        static Comparator<RecordPair> comparator = (recordPairA, recordPairB) -> {
            if(recordPairA.tick <recordPairB.tick) return -1;
            else if (recordPairA.tick ==recordPairB.tick) return 0;
            return 1;
        };

        PriorityQueue<RecordPair> queue = new PriorityQueue<>(comparator);

        @Override
        public RecordNode initialValue(long tick) {
            RecordNode n = RecordNode.ZERO;
            if(queue.peek() == null) return n;
            while(queue.peek().tick < tick) queue.poll();
            if(queue.peek() == null) return n;
            while(queue.peek().tick == tick) {
                Updated updated = queue.poll().updated;
                n = updated.applyTo(n);
            }
            return n;
        }

        @Override
        public List<Updated> peek() {
            List<Updated> ans = new LinkedList<>();
            if(queue.isEmpty()) return ans;
            long tick = queue.peek().tick;
            while(queue.peek().tick == tick) {
                Updated u = queue.poll().updated;
                ans.add(u);
            }
            return ans;
        }

        @Override
        public long peekTick() {
            if(queue.isEmpty()) return -1;
            return queue.peek().tick;
        }

        @Override
        public long pollTick() {
            if(queue.isEmpty()) return -1;
            long tick = queue.peek().tick;
            while(queue.peek().tick == tick) queue.poll();
            return tick;
        }
    }

}
