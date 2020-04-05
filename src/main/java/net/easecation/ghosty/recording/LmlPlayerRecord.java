package net.easecation.ghosty.recording;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.math.Vector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.MathUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
public class LmlPlayerRecord implements PlayerRecord {

    private RecordNode last = RecordNode.ZERO;

    private List<RecordPair> rec = new LinkedList<>();

    private String playerName;
    private Skin skin;

    public LmlPlayerRecord(BinaryStream stream) {
        this.playerName = stream.getString();
        int offset = stream.getOffset();
        try {
            this.skin = stream.getSkinLegacy();
        } catch (IllegalArgumentException e) {
            stream.setOffset(offset);
            this.skin = stream.getSkin();
        }
        int len = (int) stream.getUnsignedVarInt();
        for (int i = 0; i < len; i++) {
            RecordPair pair = new RecordPair(stream);
            rec.add(pair);
        }
    }

    public LmlPlayerRecord(Player player) {
        this.skin = player.getSkin();
        this.playerName = player.getName();
    }

    @Override
    public String getPlayerName() {
        return playerName;
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
        long lastFlags = last.getDataFlags(), flags = node.getDataFlags();
        if(lastFlags != flags)
            push(tick, UpdatedDataFlags.of(flags));
        last = node;
    }

    private void push(long tick, Updated updated) {
        rec.add(new RecordPair(tick, updated));
    }

    private class RecordPair {

        private RecordPair(BinaryStream stream) {
            try {
                this.tick = stream.getUnsignedVarInt();
                this.updated = Updated.fromBinaryStream(stream);
            } catch (Exception e) {
                Server.getInstance().getLogger().logException(e);
                throw e;
            }
        }

        private RecordPair(long tick, Updated updated) {
            this.tick = tick;
            this.updated = updated;
        }

        long tick; Updated updated;

        private void write(BinaryStream stream) {
            stream.putUnsignedVarInt((int) tick);
            stream.putByte((byte) updated.getUpdateTypeId());
            updated.write(stream);
        }
    }

    @Override
    public RecordIterator iterator() {
        LmlRecordIterator recordIterator = new LmlRecordIterator();
        rec.forEach((e) -> recordIterator.queue.offer(e));
        return recordIterator;
    }

    @Override
    public Skin getSkin() {
        return skin;
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
            while(!queue.isEmpty() && queue.peek().tick < tick) queue.poll();
            if(queue.peek() == null) return n;
            while(!queue.isEmpty() && queue.peek().tick == tick) {
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
            while(!queue.isEmpty() && queue.peek().tick == tick) {
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
            while(!queue.isEmpty() && queue.peek().tick == tick) queue.poll();
            return tick;
        }
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new BinaryStream();
        stream.putByte(PlayerRecord.OBJECT_LML);
        stream.putString(this.playerName);
        stream.putSkin(this.skin);
        stream.putUnsignedVarInt(this.rec.size());
        for (RecordPair pair : this.rec) {
            pair.write(stream);
        }
        return stream.getBuffer();
    }

    public double getMaxMovement() {
        Vector3 lastPos = null;
        double maxMovement = 0;
        for (RecordPair pair : this.rec.stream().filter(p -> p.updated instanceof UpdatedPositionXYZ).collect(Collectors.toList())) {
            UpdatedPositionXYZ pos = (UpdatedPositionXYZ) pair.updated;
            Vector3 newPos = pos.asVector3();
            if (lastPos != null) {
                double distance = newPos.distance(lastPos);
                if (distance > maxMovement) maxMovement = distance;
            }
            lastPos = newPos;
        }
        return maxMovement;
    }

    public double calculateMovementVariance() {
        Vector3 lastPos = null;

        List<RecordPair> pairs = this.rec.stream().filter(p -> p.updated instanceof UpdatedPositionXYZ).collect(Collectors.toList());
        if (pairs.size() <= 1) return 0;
        double[] distances = new double[pairs.size() - 1];
        for (int i = 0; i < pairs.size(); i++) {
            RecordPair pair = pairs.get(i);
            UpdatedPositionXYZ pos = (UpdatedPositionXYZ) pair.updated;
            Vector3 newPos = pos.asVector3();
            if (lastPos != null) {
                distances[i - 1] = newPos.distance(lastPos);
            }
            lastPos = newPos;
        }
        return MathUtil.getVariance(distances);
    }
}
