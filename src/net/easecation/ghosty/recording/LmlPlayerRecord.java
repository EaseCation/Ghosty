package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;

import java.util.*;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
class LmlPlayerRecord implements PlayerRecord{

    private RecordNode last = RecordNode.ZERO;

    private List<RecordPair> rec = new LinkedList<>();

    private Player p;

    LmlPlayerRecord(Player p) {
        this.p = p;
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

    private void push(long t, Updated u) {
        rec.add(new RecordPair(t, u));
    }

    private class RecordPair{
        private RecordPair(long t, Updated u) {
            this.t = t;
            this.u = u;
        }
        long t; Updated u;
    }

    @Override
    public RecordIterator iterator() {
        LmlRecordIterator ans = new LmlRecordIterator();
        rec.forEach((e) -> ans.q.offer(e));
        return ans;
    }

    @Override
    public Player getPlayer() {
        return p;
    }

    @Override
    public Skin getSkin() {
        return p.getSkin();
    }

    private static class LmlRecordIterator implements RecordIterator {

        static Comparator<RecordPair> cmp = (a, b) -> {
            if(a.t<b.t) return -1;
            else if (a.t==b.t) return 0;
            return 1;
        };

        PriorityQueue<RecordPair> q = new PriorityQueue<>(cmp);

        @Override
        public RecordNode initialValue(long tick) {
            RecordNode n = RecordNode.ZERO;
            if(q.peek() == null) return n;
            while(q.peek().t < tick) q.poll();
            if(q.peek() == null) return n;
            while(q.peek().t == tick) {
                Updated u = q.poll().u;
                n = u.applyTo(n);
            }
            return n;
        }

        @Override
        public List<Updated> peek() {
            List<Updated> ans = new LinkedList<>();
            if(q.isEmpty()) return ans;
            long tick = q.peek().t;
            while(q.peek().t == tick) {
                Updated u = q.poll().u;
                ans.add(u);
            }
            return ans;
        }

        @Override
        public long peekTick() {
            if(q.isEmpty()) return -1;
            return q.peek().t;
        }

        @Override
        public long pollTick() {
            if(q.isEmpty()) return -1;
            long tick = q.peek().t;
            while(q.peek().t == tick) q.poll();
            return tick;
        }
    }

}
