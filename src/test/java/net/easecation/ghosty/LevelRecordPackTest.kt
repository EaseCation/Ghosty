package net.easecation.ghosty

import cn.nukkit.block.Block
import kotlinx.serialization.json.JsonElement
import org.itxtech.synapseapi.multiprotocol.utils.block.LegacyBlockSerializer
import org.itxtech.synapseapi.multiprotocol.utils.block.VanillaBlockUpgrader
import org.itxtech.synapseapi.multiprotocol.utils.item.LegacyItemSerializer
import org.itxtech.synapseapi.multiprotocol.utils.item.VanillaItemUpgrader
import org.junit.jupiter.api.Test
import java.io.File

class LevelRecordPackTest {
    @Test
    fun `load zstd`() {
        val archivePath = System.getenv("ECPLAYBACK_TEST_ZSTD_ARCHIVE_FILE") ?: return
        Block.init()
        VanillaBlockUpgrader.initialize()
        LegacyBlockSerializer.initialize()
        VanillaItemUpgrader.initialize()
        LegacyItemSerializer.initialize()
        LevelRecordPack.unpack<JsonElement>(
            File(archivePath).inputStream(),
            format = Format.TAR_ZSTD
        )
    }
}
