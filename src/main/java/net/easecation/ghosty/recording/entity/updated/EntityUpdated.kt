package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import net.easecation.ghosty.entity.SimulatedEntity
import net.easecation.ghosty.recording.entity.EntityRecordNode

@Serializable
sealed interface EntityUpdated {
    /**
     * Process to the playback entity. 回放时应用于实体
     * @param entity Simulated entity
     */
    fun processTo(entity: SimulatedEntity): Unit = entity.close()

    /**
     * Apply to the record node. 应用到RecordNode中
     * @param node the node to be applied. 要应用的节点
     * @return self. 返回自身
     */
    fun applyTo(node: EntityRecordNode): EntityRecordNode = node

    fun getUpdateTypeId(): Int

    companion object {
        /**
         * Create a PlayerUpdated from the stream. 从流中创建一个PlayerUpdated
         * @param stream BinaryStream
         * @return PlayerUpdated
         */
        @JvmStatic
        fun fromBinaryStream(stream: BinaryStream, formatVersion: Int): EntityUpdated? {
            return when (stream.byte) {
                TYPE_POSITION_XYZ -> EntityUpdatedPositionXYZ.ADAPTER.read(stream)
                TYPE_ROTATION -> EntityUpdatedRotation.ADAPTER.read(stream)
                TYPE_TAG_NAME -> EntityUpdatedTagName.ADAPTER.read(stream)
                TYPE_SCORE_TAG -> EntityUpdatedScoreTag.ADAPTER.read(stream)
                TYPE_DATA_FLAGS -> EntityUpdatedDataFlags.ADAPTER.read(stream)
                TYPE_ITEM -> EntityUpdatedItem.ADAPTER.read(stream, formatVersion)
                TYPE_SCALE -> EntityUpdatedScale.ADAPTER.read(stream)
                TYPE_CLOSE -> EntityUpdatedClose.ADAPTER.read(stream)
                TYPE_NAMETAG_ALWAYS_VISIBLE -> EntityUpdatedNameTagAlwaysVisible.ADAPTER.read(stream)
                TYPE_SKIN_ID -> EntityUpdatedSkinId.ADAPTER.read(stream)
                TYPE_NPC_SKIN_ID -> EntityUpdatedNPCSkinId.ADAPTER.read(stream)
                TYPE_VARIANT -> EntityUpdatedVariant.ADAPTER.read(stream)
                TYPE_MARK_VARIANT -> EntityUpdatedMarkVariant.ADAPTER.read(stream)
                TYPE_SKIN_INFO -> EntityUpdatedSkinInfo.ADAPTER.read(stream)
                else -> null
            }
        }

        /**
         * Create a PlayerUpdated from the stream. 从流中创建一个PlayerUpdated
         * @param stream BinaryStream
         * @return PlayerUpdated
         */
        @JvmStatic
        fun writeBinaryStream(updated: EntityUpdated, stream: BinaryStream) {
            when (updated) {
                is EntityUpdatedPositionXYZ -> EntityUpdatedPositionXYZ.ADAPTER.write(updated, stream)
                is EntityUpdatedRotation -> EntityUpdatedRotation.ADAPTER.write(updated, stream)
                is EntityUpdatedTagName -> EntityUpdatedTagName.ADAPTER.write(updated, stream)
                is EntityUpdatedScoreTag -> EntityUpdatedScoreTag.ADAPTER.write(updated, stream)
                is EntityUpdatedDataFlags -> EntityUpdatedDataFlags.ADAPTER.write(updated, stream)
                is EntityUpdatedItem -> EntityUpdatedItem.ADAPTER.write(updated, stream)
                is EntityUpdatedScale -> EntityUpdatedScale.ADAPTER.write(updated, stream)
                is EntityUpdatedClose -> EntityUpdatedClose.ADAPTER.write(updated, stream)
                is EntityUpdatedNameTagAlwaysVisible -> EntityUpdatedNameTagAlwaysVisible.ADAPTER.write(updated, stream)
                is EntityUpdatedSkinId -> EntityUpdatedSkinId.ADAPTER.write(updated, stream)
                is EntityUpdatedNPCSkinId -> EntityUpdatedNPCSkinId.ADAPTER.write(updated, stream)
                is EntityUpdatedVariant -> EntityUpdatedVariant.ADAPTER.write(updated, stream)
                is EntityUpdatedMarkVariant -> EntityUpdatedMarkVariant.ADAPTER.write(updated, stream)
                is EntityUpdatedSkinInfo -> EntityUpdatedSkinInfo.ADAPTER.write(updated, stream)
            }
        }

        fun serializer(updated: EntityUpdated): KSerializer<*> {
            return when (updated) {
                is EntityUpdatedPositionXYZ -> EntityUpdatedPositionXYZ.serializer()
                is EntityUpdatedRotation -> EntityUpdatedRotation.serializer()
                is EntityUpdatedTagName -> EntityUpdatedTagName.serializer()
                is EntityUpdatedScoreTag -> EntityUpdatedScoreTag.serializer()
                is EntityUpdatedDataFlags -> EntityUpdatedDataFlags.serializer()
                is EntityUpdatedItem -> EntityUpdatedItem.serializer()
                is EntityUpdatedScale -> EntityUpdatedScale.serializer()
                is EntityUpdatedClose -> EntityUpdatedClose.serializer()
                is EntityUpdatedNameTagAlwaysVisible -> EntityUpdatedNameTagAlwaysVisible.serializer()
                is EntityUpdatedSkinId -> EntityUpdatedSkinId.serializer()
                is EntityUpdatedNPCSkinId -> EntityUpdatedNPCSkinId.serializer()
                is EntityUpdatedVariant -> EntityUpdatedVariant.serializer()
                is EntityUpdatedMarkVariant -> EntityUpdatedMarkVariant.serializer()
                is EntityUpdatedSkinInfo -> EntityUpdatedSkinInfo.serializer()
            }
        }

        const val TYPE_POSITION_XYZ: Int = 0
        const val TYPE_ROTATION: Int = 1
        const val TYPE_TAG_NAME: Int = 2
        const val TYPE_SCORE_TAG: Int = 3
        const val TYPE_DATA_FLAGS: Int = 4
        const val TYPE_ITEM: Int = 5
        const val TYPE_SCALE: Int = 6
        const val TYPE_CLOSE: Int = 7
        const val TYPE_NAMETAG_ALWAYS_VISIBLE: Int = 8
        const val TYPE_SKIN_ID: Int = 9
        const val TYPE_NPC_SKIN_ID: Int = 10
        const val TYPE_VARIANT: Int = 11
        const val TYPE_MARK_VARIANT: Int = 12
        const val TYPE_SKIN_INFO: Int = 13
        const val MAX_TYPE_ID: Int = 14
    }
}


interface UpdateAdapter<T: EntityUpdated> {
    /**
     * 如果有状态，则表示需要在回退时寻找更早之前的该状态，进行应用
     * @return whether this Updated has states. 是否有状态
     */
    fun hasStates(): Boolean

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
