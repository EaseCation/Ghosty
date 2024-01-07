package net.easecation.ghosty.recording.entity;

import cn.nukkit.Server;
import cn.nukkit.entity.Entity;
import cn.nukkit.item.Item;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.GhostyPlugin;
import net.easecation.ghosty.RecordIterator;
import net.easecation.ghosty.recording.entity.updated.*;

import java.util.*;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:34.
 */
public class EntityRecordImpl implements EntityRecord {

    private EntityRecordNode last = EntityRecordNode.ZERO;

    private final List<RecordPair> rec = new LinkedList<>();

    private final long entityId;
    private final int networkId;

    public EntityRecordImpl(BinaryStream stream) {
        this.entityId = stream.getEntityUniqueId();
        this.networkId = stream.getVarInt();
        int len = (int) stream.getUnsignedVarInt();
        for (int i = 0; i < len; i++) {
            RecordPair pair = new RecordPair(stream);
            rec.add(pair);
        }
    }

    public EntityRecordImpl(Entity entity) {
        this.entityId = entity.getId();
        this.networkId = entity.getNetworkId();
    }

    @Override
    public long getEntityId() {
        return entityId;
    }

    @Override
    public int getNetworkId() {
        return networkId;
    }

    @Override
    public byte[] toBinary() {
        BinaryStream stream = new BinaryStream();
        stream.putByte(EntityRecord.OBJECT_V0);
        stream.putEntityUniqueId(this.entityId);
        stream.putVarInt(this.networkId);
        stream.putUnsignedVarInt(this.rec.size());
        for (RecordPair pair : this.rec) {
            pair.write(stream);
        }
        return stream.getBuffer();
    }

    @Override
    public void record(int tick, EntityRecordNode node) {
        double lx = last.getX(), x = node.getX();
        double ly = last.getY(), y = node.getY();
        double lz = last.getZ(), z = node.getZ();
        if (lx != x || ly != y || lz != z) {
            push(tick, EntityUpdatedPositionXYZ.of(x, y, z));
        }
        double la = last.getYaw(), a = node.getYaw();
        double lp = last.getPitch(), p = node.getPitch();
        if (la != a || lp != p) {
            push(tick, EntityUpdatedRotation.of(a, p));
        }
        String ln = last.getTagName(), n = node.getTagName();
        if (!Objects.equals(ln, n)) {
            push(tick, EntityUpdatedTagName.of(n));
        }
        String ls = last.getScoreTag(), s = node.getScoreTag();
        if (!Objects.equals(ls, s)) {
            push(tick, EntityUpdatedScoreTag.of(s));
        }
        Item li = last.getItem(), i = node.getItem();
        if (!Objects.equals(li, i)) {
            push(tick, EntityUpdatedItem.of(i));
        }
        if (li != null && i != null && li.getDamage() != i.getDamage()) {
            push(tick, EntityUpdatedItem.of(i));
        }
        long lastFlags = last.getDataFlags(), flags = node.getDataFlags();
        if (lastFlags != flags) {
            push(tick, EntityUpdatedDataFlags.of(flags));
        }
        float lastScale = last.getScale(), scale = node.getScale();
        if (lastScale != scale) {
            push(tick, EntityUpdatedScale.of(scale));
        }
        boolean lastNameTagAlwaysVisible = last.isNameTagAlwaysVisible(), nameTagAlwaysVisible = node.isNameTagAlwaysVisible();
        if (lastNameTagAlwaysVisible != nameTagAlwaysVisible) {
            push(tick, EntityUpdatedNameTagAlwaysVisible.of(nameTagAlwaysVisible));
        }
        last = node;
    }

    @Override
    public void recordClose(int tick) {
        push(tick, EntityUpdatedClose.of());
    }

    private void push(int tick, EntityUpdated updated) {
        rec.add(new RecordPair(tick, updated));
        if (updated.getUpdateTypeId() != EntityUpdated.TYPE_POSITION_XYZ && updated.getUpdateTypeId() != EntityUpdated.TYPE_ROTATION) {
            GhostyPlugin.getInstance().getLogger().debug("entity[" + this.entityId + "] " + tick + " -> " + updated);
        }
    }

    /**
     * tick 与 PlayerUpdated 对的封装类
     */
    private static class RecordPair {

        private RecordPair(BinaryStream stream) {
            try {
                this.tick = (int) stream.getUnsignedVarInt();
                this.updated = EntityUpdated.fromBinaryStream(stream);
            } catch (Exception e) {
                Server.getInstance().getLogger().logException(e);
                throw e;
            }
        }

        private RecordPair(int tick, EntityUpdated updated) {
            this.tick = tick;
            this.updated = updated;
        }

        int tick; EntityUpdated updated;

        private void write(BinaryStream stream) {
            stream.putUnsignedVarInt(tick);
            stream.putByte((byte) updated.getUpdateTypeId());
            updated.write(stream);
        }
    }

    @Override
    public RecordIterator<EntityRecordNode, EntityUpdated> iterator() {
        EntityRecordIterator recordIterator = new EntityRecordIterator();
        rec.forEach((e) -> recordIterator.queue.offer(e));
        return recordIterator;
    }

    /**
     * LmlRecordIterator是一个实现了RecordIterator接口的私有静态类。
     * 它使用一个优先队列来存储和管理RecordPair对象。
     * 优先队列根据RecordPair对象的tick值进行排序。
     */
    private static class EntityRecordIterator implements RecordIterator<EntityRecordNode, EntityUpdated> {

        /**
         * 一个静态比较器，用于根据RecordPair对象的tick值进行排序。
         */
        static Comparator<RecordPair> comparator = (recordPairA, recordPairB) -> {
            if(recordPairA.tick <recordPairB.tick) return -1;
            else if (recordPairA.tick ==recordPairB.tick) return 0;
            return 1;
        };

        /**
         * 一个优先队列，用于存储和管理RecordPair对象。
         * 优先队列根据RecordPair对象的tick值进行排序。
         */
        PriorityQueue<RecordPair> queue = new PriorityQueue<>(comparator);

        /**
         * 根据给定的tick值返回RecordNode的初始值。
         * 它将所有具有相同tick值的更新应用于RecordNode。
         * @param tick 要基于的tick值。
         * @return RecordNode的初始值。
         */
        @Override
        public EntityRecordNode initialValue(int tick) {
            EntityRecordNode n = EntityRecordNode.ZERO;
            if (queue.peek() == null) return n;
            while (!queue.isEmpty() && queue.peek().tick < tick) queue.poll();
            if (queue.peek() == null) return n;
            while (!queue.isEmpty() && queue.peek().tick == tick) {
                EntityUpdated updated = queue.poll().updated;
                n = updated.applyTo(n);
            }
            return n;
        }

        /**
         * 返回具有与队列中第一个对象相同tick值的Updated对象列表。
         * @return Updated对象列表。
         */
        @Override
        public List<EntityUpdated> peek() {
            List<EntityUpdated> ans = new LinkedList<>();
            if(queue.isEmpty()) return ans;
            int tick = queue.peek().tick;
            PriorityQueue<RecordPair> tempQueue = new PriorityQueue<>(queue);
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
            while (!queue.isEmpty() && queue.peek().tick == tick) queue.poll();
            return tick;
        }
    }

}
