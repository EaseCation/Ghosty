package net.easecation.ghosty.serializer

import cn.nukkit.block.Block
import cn.nukkit.item.Item
import cn.nukkit.math.BlockVector3
import com.github.avrokotlin.avro4k.Avro
import com.github.avrokotlin.avro4k.schema
import com.google.gson.JsonArray as GsonArray
import com.google.gson.JsonObject as GsonObject
import kotlinx.serialization.decodeFromByteArray
import kotlinx.serialization.encodeToByteArray
import kotlinx.serialization.encodeToHexString
import kotlinx.serialization.json.JsonElement
import net.easecation.ghosty.Format
import net.easecation.ghosty.LevelRecordPack
import net.easecation.ghosty.recording.entity.updated.EntityUpdated
import net.easecation.ghosty.recording.entity.updated.EntityUpdatedItem
import net.easecation.ghosty.recording.level.updated.LevelUpdated
import net.easecation.ghosty.recording.level.updated.LevelUpdatedBlockChange
import net.easecation.ghosty.recording.level.updated.LevelUpdatedCustom
import net.easecation.ghosty.recording.player.updated.PlayerUpdated
import org.itxtech.synapseapi.multiprotocol.utils.block.LegacyBlockSerializer
import org.itxtech.synapseapi.multiprotocol.utils.block.VanillaBlockUpgrader
import org.itxtech.synapseapi.multiprotocol.utils.item.LegacyItemSerializer
import org.itxtech.synapseapi.multiprotocol.utils.item.VanillaItemUpgrader
import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.TimeSource

class SerializerTest {
    @Test
    fun `test item avro serialize`() {
        println(Avro.schema<EntityUpdatedItem>().toString())
        val values = listOf(
            EntityUpdatedItem(Item(Item.PLANKS, 0, 64)),
            EntityUpdatedItem(Item(Item.IRON_AXE, 5)),
            EntityUpdatedItem(Item(Item.IRON_AXE, 0, 1)),
            EntityUpdatedItem(Item(Item.IRON_AXE, 0, 1, "MyIronAxe")),
        )
        values.forEach { value ->
            val encoded = Avro.encodeToByteArray(value)
            val decoded = Avro.decodeFromByteArray<EntityUpdatedItem>(encoded)
            assertEquals(decoded, value)
        }
    }

    @Test
    fun `test gson avro serialize`() {
        println(Avro.schema<LevelUpdatedCustom>().toString())
        val values = listOf(
            LevelUpdatedCustom(GsonObject().apply {
                addProperty("a", 234)
                addProperty("b", "Hello")
            }),
            LevelUpdatedCustom(GsonObject()),
            LevelUpdatedCustom(GsonObject().apply {
                add("a", GsonArray().apply {
                    add("b")
                    add("c")
                    add("d")
                    add("e")
                })
                add("a", GsonArray().apply {
                    add("b")
                    add(1)
                    add(1.3)
                    add('c')
                })
            }),
        )
        values.forEach { value ->
            val encoded = Avro.encodeToByteArray(value)
            val decoded = Avro.decodeFromByteArray<LevelUpdatedCustom>(encoded)
            assertEquals(decoded, value)
        }
    }

    @Test
    fun `test block avro serialize`() {
        Block.init()
        println(Avro.schema<LevelUpdatedBlockChange>().toString())
        val values = listOf(
            LevelUpdatedBlockChange(
                pos = BlockVector3(114, 514, 191),
                block = Block.get(Block.PLANKS, 1),
            ),
        )
        values.forEach { value ->
            val encoded = Avro.encodeToByteArray(value)
            val decoded = Avro.decodeFromByteArray<LevelUpdatedBlockChange>(encoded)
            assertEquals(decoded, value)
        }
    }

    @Test
    fun testUnpack() {
        val archivePath = System.getenv("ECPLAYBACK_TEST_ARCHIVE_FILE") ?: return
        val archiveFile = File(archivePath)
        if (!archiveFile.exists()) {
            throw IllegalArgumentException(
                "The value of `ECPLAYBACK_TEST_ARCHIVE_FILE` is not a valid path, the file does not exist"
            )
        }

        Block.init()
        VanillaBlockUpgrader.initialize()
        LegacyBlockSerializer.initialize()
        VanillaItemUpgrader.initialize()
        LegacyItemSerializer.initialize()

        val startInstant = TimeSource.Monotonic.markNow()
        val pack = LevelRecordPack.unpack<JsonElement>(archiveFile.inputStream(), Format.ZIP)
        var entityRecordsCount = 0
        pack.entityRecords.forEach { record ->
            val iterator = record.iterator()
            do {
                iterator.peek().forEach {
                    Avro.schema(EntityUpdated.serializer(it))
                    Avro.encodeToByteArray(it)
                    entityRecordsCount++
                }
            } while (iterator.pollTick() > 0)
        }

        var playerRecordsCount = 0
        pack.playerRecords.forEach { record ->
            val iterator = record.iterator()
            do {
                iterator.peek().forEach {
                    println(Avro.schema(PlayerUpdated.serializer(it)))
                    println(Avro.encodeToHexString(it))
                }
            } while (iterator.pollTick() > 0)
            playerRecordsCount++
        }

        var levelRecordsCount = 0
        val iterator = pack.levelRecord.iterator()
        do {
            iterator.peek().forEach {
                Avro.schema(LevelUpdated.serializer(it))
                Avro.encodeToByteArray(it)
                levelRecordsCount++
            }
        } while (iterator.pollTick() > 0)
        val elapsed = startInstant.elapsedNow()
        println("""
            Records count report:
            Entity: ${pack.entityRecords.size}
            Player: ${pack.playerRecords.size}
            Level: ${pack.playerRecords.size}
            $elapsed costed
        """.trimIndent())
    }
}
