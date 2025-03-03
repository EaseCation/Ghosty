package net.easecation.ghosty.recording.entity.updated

import cn.nukkit.utils.BinaryStream
import kotlinx.serialization.Serializable

@Serializable
data object EntityUpdatedClose : EntityUpdated {
    val ADAPTER: UpdateAdapter<EntityUpdatedClose> = Adapter
    override fun getUpdateTypeId(): Int = EntityUpdated.TYPE_CLOSE

    @JvmStatic
    fun of() = EntityUpdatedClose

    private object Adapter: UpdateAdapter<EntityUpdatedClose> {
        override fun hasStates(): Boolean = false
        override fun write(updated: EntityUpdatedClose, stream: BinaryStream) = Unit
        override fun read(stream: BinaryStream) = EntityUpdatedClose
    }
}
