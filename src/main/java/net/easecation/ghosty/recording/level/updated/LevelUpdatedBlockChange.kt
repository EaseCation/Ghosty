package net.easecation.ghosty.recording.level.updated

import cn.nukkit.block.Block
import cn.nukkit.math.BlockVector3
import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable
import net.easecation.ghosty.recording.level.LevelRecordNode
import net.easecation.ghosty.serializer.NukkitBlockSerializer
import net.easecation.ghosty.serializer.NukkitBlockVector3Serializer
import net.easecation.ghosty.util.PersistenceBinaryStreamHelper

@Serializable
data class LevelUpdatedBlockChange(
    @Serializable(with = NukkitBlockVector3Serializer::class)
    val pos: BlockVector3,
    @Serializable(with = NukkitBlockSerializer::class)
    val block: Block,
) : LevelUpdated {

    override fun getUpdateTypeId(): Int = LevelUpdated.TYPE_BLOCK_CHANGE

    override fun processTo(node: LevelRecordNode) {
        node.handleBlockChange(pos, block)
    }

    override fun backwardTo(node: LevelRecordNode) = Unit

    companion object {
        @JvmStatic
        fun of(pos: BlockVector3, block: Block) = LevelUpdatedBlockChange(pos, block)

        @JvmStatic
        val ADAPTER: LevelUpdateAdapter<LevelUpdatedBlockChange> = Adapter
    }

    private object Adapter : LevelUpdateAdapter<LevelUpdatedBlockChange> {
        override fun write(updated: LevelUpdatedBlockChange, stream: BinaryStream) {
            stream.putBlockVector3(updated.pos)
            PersistenceBinaryStreamHelper.putBlock(stream, updated.block)
        }

        override fun read(stream: BinaryStream, formatVersion: Int): LevelUpdatedBlockChange {
            return when (formatVersion) {
                1, 2 -> LevelUpdatedBlockChange(
                    pos = stream.blockVector3,
                    block = PersistenceBinaryStreamHelper.getBlock(stream)
                )

                0 -> {
                    val pos = stream.blockVector3
                    val block = Block.get(stream.varInt, stream.byte.toInt())
                    LevelUpdatedBlockChange(pos, block)
                }

                else -> throw IllegalArgumentException("Unsupported format version: $formatVersion")
            }
        }
    }
}