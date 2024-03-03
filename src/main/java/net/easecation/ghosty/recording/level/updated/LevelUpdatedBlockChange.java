package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.block.Block;
import cn.nukkit.math.BlockVector3;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper;

public class LevelUpdatedBlockChange implements LevelUpdated {

    private BlockVector3 pos;
    private Block block;

    private LevelUpdatedBlockChange(BlockVector3 pos, Block block) {
        this.pos = pos;
        this.block = block;
    }

    public LevelUpdatedBlockChange(BinaryStream stream, int formatVersion) {
        switch (formatVersion) {
            case 1, 2: {
                this.pos = stream.getBlockVector3();
                this.block = PersistenceBinaryStreamHelper.getBlock(stream);
                break;
            }
            case 0: {
                this.read(stream);
                break;
            }
            default:
                throw new IllegalArgumentException("Unsupported format version: " + formatVersion);
        }
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
    public void backwardTo(LevelRecordNode node) {
        // TODO 回退方块变更
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putBlockVector3(pos);
        PersistenceBinaryStreamHelper.putBlock(stream, block);
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
