package net.easecation.ghosty.recording.player.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.PlaybackNPC
import net.easecation.ghosty.recording.player.PlayerRecordNode

@Serializable
sealed interface PlayerUpdated {
    /**
     * @return the type id of this Updated.
     */
    fun getUpdateTypeId(): Int

    /**
     * Process to the playback entity. 回放时应用于实体
     * @param ghost Ghost entity
     */
    fun processTo(ghost: PlaybackNPC) = Unit

    /**
     * Apply to the record node. 应用到RecordNode中
     * @param node the node to be applied. 要应用的节点
     * @return self. 返回自身
     */
    fun applyTo(node: PlayerRecordNode): PlayerRecordNode = node

    companion object {
        /**
         * Create a PlayerUpdated from the stream. 从流中创建一个PlayerUpdated
         * @param stream BinaryStream
         * @return PlayerUpdated
         */
        @JvmStatic
        fun fromBinaryStream(stream: BinaryStream, formatVersion: Int): PlayerUpdated {
            return when (val updatedType = stream.byte) {
                TYPE_POSITION_XYZ -> PlayerUpdatedPositionXYZ.ADAPTER.read(stream)
                TYPE_ROTATION -> PlayerUpdatedRotation.ADAPTER.read(stream)
                TYPE_TAG_NAME -> PlayerUpdatedTagName.ADAPTER.read(stream)
                TYPE_WORLD -> PlayerUpdatedWorldChanged.ADAPTER.read(stream)
                TYPE_DATA_FLAGS -> PlayerUpdatedDataFlags.ADAPTER.read(stream)
                TYPE_ITEM -> PlayerUpdatedItem.ADAPTER.read(stream, formatVersion)
                TYPE_ANIMATE -> PlayerUpdatedAnimate.ADAPTER.read(stream)
                TYPE_ENTITY_EVENT -> PlayerUpdatedEntityEvent.ADAPTER.read(stream)
                TYPE_ARMOR_0 -> PlayerUpdatedArmor0.ADAPTER.read(stream, formatVersion)
                TYPE_ARMOR_1 -> PlayerUpdatedArmor1.ADAPTER.read(stream, formatVersion)
                TYPE_ARMOR_2 -> PlayerUpdatedArmor2.ADAPTER.read(stream, formatVersion)
                TYPE_ARMOR_3 -> PlayerUpdatedArmor3.ADAPTER.read(stream, formatVersion)
                TYPE_OFFHAND -> PlayerUpdatedOffhand.ADAPTER.read(stream, formatVersion)
                TYPE_TAKE_ITEM_ENTITY -> PlayerUpdatedTakeItemEntity.ADAPTER.read(stream)
                TYPE_PING -> PlayerUpdatedPing.ADAPTER.read(stream)
                TYPE_ATTACK -> PlayerUpdatedAttack.ADAPTER.read(stream)
                TYPE_MOTION -> PlayerUpdatedMotion.ADAPTER.read(stream)
                else -> throw IllegalArgumentException("Unsupported type id: $updatedType")
            }
        }

        @JvmStatic
        fun writeBinaryStream(updated: PlayerUpdated , stream: BinaryStream) {
            when (updated) {
                is PlayerUpdatedPositionXYZ -> PlayerUpdatedPositionXYZ.ADAPTER.write(updated, stream)
                is PlayerUpdatedRotation -> PlayerUpdatedRotation.ADAPTER.write(updated, stream)
                is PlayerUpdatedTagName -> PlayerUpdatedTagName.ADAPTER.write(updated, stream)
                is PlayerUpdatedWorldChanged -> PlayerUpdatedWorldChanged.ADAPTER.write(updated, stream)
                is PlayerUpdatedDataFlags -> PlayerUpdatedDataFlags.ADAPTER.write(updated, stream)
                is PlayerUpdatedItem -> PlayerUpdatedItem.ADAPTER.write(updated, stream)
                is PlayerUpdatedAnimate -> PlayerUpdatedAnimate.ADAPTER.write(updated, stream)
                is PlayerUpdatedEntityEvent -> PlayerUpdatedEntityEvent.ADAPTER.write(updated, stream)
                is PlayerUpdatedArmor0 -> PlayerUpdatedArmor0.ADAPTER.write(updated, stream)
                is PlayerUpdatedArmor1 -> PlayerUpdatedArmor1.ADAPTER.write(updated, stream)
                is PlayerUpdatedArmor2 -> PlayerUpdatedArmor2.ADAPTER.write(updated, stream)
                is PlayerUpdatedArmor3 -> PlayerUpdatedArmor3.ADAPTER.write(updated, stream)
                is PlayerUpdatedOffhand -> PlayerUpdatedOffhand.ADAPTER.write(updated, stream)
                is PlayerUpdatedTakeItemEntity -> PlayerUpdatedTakeItemEntity.ADAPTER.write(updated, stream)
                is PlayerUpdatedPing -> PlayerUpdatedPing.ADAPTER.write(updated, stream)
                is PlayerUpdatedAttack -> PlayerUpdatedAttack.ADAPTER.write(updated, stream)
                is PlayerUpdatedMotion -> PlayerUpdatedMotion.ADAPTER.write(updated, stream)
            }
        }
        @JvmStatic
        fun serializer(updated: PlayerUpdated): KSerializer<*> {
            return when (updated) {
                is PlayerUpdatedAnimate -> PlayerUpdatedAnimate.serializer()
                is PlayerUpdatedArmor0 -> PlayerUpdatedArmor0.serializer()
                is PlayerUpdatedArmor1 -> PlayerUpdatedArmor1.serializer()
                is PlayerUpdatedArmor2 -> PlayerUpdatedArmor2.serializer()
                is PlayerUpdatedArmor3 -> PlayerUpdatedArmor3.serializer()
                is PlayerUpdatedAttack -> PlayerUpdatedAttack.serializer()
                is PlayerUpdatedDataFlags -> PlayerUpdatedDataFlags.serializer()
                is PlayerUpdatedEntityEvent -> PlayerUpdatedEntityEvent.serializer()
                is PlayerUpdatedItem -> PlayerUpdatedItem.serializer()
                is PlayerUpdatedMotion -> PlayerUpdatedMotion.serializer()
                is PlayerUpdatedOffhand -> PlayerUpdatedOffhand.serializer()
                is PlayerUpdatedPing -> PlayerUpdatedPing.serializer()
                is PlayerUpdatedPositionXYZ -> PlayerUpdatedPositionXYZ.serializer()
                is PlayerUpdatedRotation -> PlayerUpdatedRotation.serializer()
                is PlayerUpdatedTagName -> PlayerUpdatedTagName.serializer()
                is PlayerUpdatedTakeItemEntity -> PlayerUpdatedTakeItemEntity.serializer()
                is PlayerUpdatedWorldChanged -> PlayerUpdatedWorldChanged.serializer()
            }
        }

        const val TYPE_POSITION_XYZ: Int = 0
        const val TYPE_ROTATION: Int = 1
        const val TYPE_TAG_NAME: Int = 2
        const val TYPE_WORLD: Int = 3
        const val TYPE_DATA_FLAGS: Int = 4
        const val TYPE_ITEM: Int = 5
        const val TYPE_ARMOR_0: Int = 6
        const val TYPE_ARMOR_1: Int = 7
        const val TYPE_ARMOR_2: Int = 8
        const val TYPE_ARMOR_3: Int = 9
        const val TYPE_OFFHAND: Int = 10
        const val TYPE_ANIMATE: Int = 11
        const val TYPE_ENTITY_EVENT: Int = 12
        const val TYPE_TAKE_ITEM_ENTITY: Int = 13
        const val TYPE_PING: Int = 14
        const val TYPE_ATTACK: Int = 15
        const val TYPE_MOTION: Int = 16
        const val MAX_TYPE_ID: Int = 16
    }
}

interface PlayerUpdateAdapter<T: PlayerUpdated> {
    /**
     * Write to the stream. 写入到流中
     * @param stream BinaryStream
     */
    fun write(updated: T, stream: BinaryStream)

    /**
     * Read from the stream. 从流中读取
     * @param stream BinaryStream
     */
    fun read(stream: BinaryStream): T
    fun read(stream: BinaryStream, version: Int): T = read(stream)
}
