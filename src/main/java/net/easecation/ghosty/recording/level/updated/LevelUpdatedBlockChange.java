package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.block.Block;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedBlockChange implements LevelUpdated {

    private BlockVector3 pos;
    private Block block;

    private LevelUpdatedBlockChange(BlockVector3 pos, Block block) {
        this.pos = pos;
        this.block = block;
    }

    public LevelUpdatedBlockChange(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedBlockChange of(BlockVector3 pos, Block block) {
        return new LevelUpdatedBlockChange(pos, block);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_BLOCK_CHANGE;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.handleBlockChange(pos, block);
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putBlockVector3(pos);
        stream.putVarInt(block.getId());
        stream.putByte((byte) block.getDamage());
    }

    @Override
    public void read(BinaryStream stream) {
        this.pos = stream.getBlockVector3();
        this.block = Block.get(stream.getVarInt(), stream.getByte());
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedBlockChange o)) return false;
        return (pos.equals(o.pos)) && (block.equals(o.block));
    }

    @Override
    public String toString() {
        return "LevelUpdatedBlockChange{" +
                "pos=" + pos +
                ", block=" + block +
                '}';
    }

    @Override
    public int hashCode() {
        return pos.hashCode() ^ block.hashCode();
    }
}
