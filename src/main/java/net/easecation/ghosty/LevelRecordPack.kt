package net.easecation.ghosty

import cn.nukkit.level.Level
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.serializer
import net.easecation.ghosty.playback.LevelPlaybackEngine
import net.easecation.ghosty.recording.entity.EntityRecord
import net.easecation.ghosty.recording.level.LevelRecord
import net.easecation.ghosty.recording.player.PlayerRecord
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorInputStream
import org.apache.commons.compress.compressors.zstandard.ZstdCompressorOutputStream
import java.io.*

private val json = Json {
    ignoreUnknownKeys = true
}

enum class Format {
    ZIP,
    TAR_ZSTD,
    ;

    fun getExtension() = when (this) {
        ZIP -> ".zip"
        TAR_ZSTD -> ".tar.zstd"
    }
}

class LevelRecordPack<in T : Any> @JvmOverloads constructor(
    val levelRecord: LevelRecord,
    val playerRecords: List<PlayerRecord>,
    val entityRecords: List<EntityRecord>,
    val metadataSerializer: KSerializer<@UnsafeVariance T>,
    // 元数据，不参与任何的回放和录制逻辑，但是可以用于存储一些额外的信息，比如录制的地图名称，录制时间等
    var metadata: @UnsafeVariance T? = null,
) {
    @Throws(IOException::class)
    fun pack(outputStream: OutputStream) {
        TarArchiveOutputStream(ZstdCompressorOutputStream(outputStream.buffered(), 19)).use { zos ->
            fun insert(name: String, binary: ByteArray) {
                val entry = TarArchiveEntry(name)
                entry.size = binary.size.toLong()
                zos.putArchiveEntry(entry)
                zos.write(binary)
                zos.closeArchiveEntry()
            }

            insert("level_record.ecrecl", levelRecord.toBinary())

            playerRecords.forEachIndexed { i, playerRecord ->
                insert("player/player_record_${i}_${playerRecord.playerName}.ecrecp", playerRecord.toBinary())
            }
            entityRecords.forEachIndexed { i, entityRecord ->
                insert("player/player_record_${i}_${entityRecord.entityId}.ecrecp", entityRecord.toBinary())
            }

            // Metadata
            val metadata = json.encodeToString(metadataSerializer, metadata!!).encodeToByteArray()
            insert("metadata.json", metadata)

            zos.finish()
        }
    }

    /**
     * Pack the level record and player records to a compress file. (Blocking)
     * 保存录制的记录到一个压缩文件中（阻塞）
     * @param file The compress file to save to. 保存到的压缩文件
     * @throws IOException If an I/O error has occurred. 如果发生了IO错误
     */
    @Throws(IOException::class)
    fun packFile(file: File) {
        if (!file.parentFile.isDirectory) {
            file.parentFile.mkdirs()
        }
        this.pack(FileOutputStream(file))
    }

    fun createPlayback(level: Level?): LevelPlaybackEngine {
        return LevelPlaybackEngine(levelRecord, level, playerRecords, entityRecords)
    }

    companion object {
        @Throws(IOException::class)
        @JvmStatic
        fun <MT : Any> unpack(
            inputStream: InputStream,
            format: Format,
            metadataSerializer: KSerializer<MT>,
        ): LevelRecordPack<MT> {
            var levelRecord: LevelRecord? = null
            val playerRecords = ArrayList<PlayerRecord>()
            val entityRecords = ArrayList<EntityRecord>()
            var metadata: MT? = null

            val archiveInput = when (format) {
                Format.ZIP -> ZipArchiveInputStream(inputStream.buffered())
                Format.TAR_ZSTD -> TarArchiveInputStream(ZstdCompressorInputStream(inputStream.buffered()))
            }

            archiveInput.use { zis ->
                zis.forEach { entry ->
                    if (entry.isDirectory) return@forEach
                    when {
                        entry.name.startsWith("level_record") -> {
                            val readAllBytes = zis.readAllBytes()
                            levelRecord = LevelRecord.fromBinary(readAllBytes)
                        }

                        entry.name.startsWith("player/") -> {
                            playerRecords.add(PlayerRecord.fromBinary(zis.readAllBytes()))
                        }

                        entry.name.startsWith("entity/") -> {
                            entityRecords.add(EntityRecord.fromBinary(zis.readAllBytes()))
                        }

                        entry.name == "metadata.json" -> {
                            val decoded = zis.readAllBytes().decodeToString()
                            metadata = json.decodeFromString(metadataSerializer, decoded)
                        }
                    }
                }
            }

            return LevelRecordPack(
                levelRecord = levelRecord ?: throw IOException("No level record found in the $format file."),
                playerRecords = playerRecords,
                entityRecords = entityRecords,
                metadata = metadata,
                metadataSerializer = metadataSerializer,
            )
        }

        @Throws(IOException::class)
        @JvmStatic
        inline fun <reified MT : Any> unpack(inputStream: InputStream, format: Format): LevelRecordPack<MT> =
            unpack(inputStream, format, serializer())

        /**
         * Load a level record pack from a compress file. (Blocking)
         * 从一个压缩文件中加载一个LevelRecordPack（阻塞）
         * @param file The compress file to load from. 从中加载的压缩文件
         * @return The loaded level record pack. 加载的LevelRecordPack
         * @throws IOException If an I/O error has occurred. 如果发生了IO错误
         */
        @Throws(IOException::class)
        @JvmStatic
        fun <MT : Any> unpackFile(
            file: File,
            format: Format,
            metadataSerializer: KSerializer<MT>,
        ): LevelRecordPack<MT> =
            unpack(FileInputStream(file), format, metadataSerializer)

        @Throws(IOException::class)
        @JvmStatic
        inline fun <reified MT : Any> unpackFile(file: File, format: Format): LevelRecordPack<MT> =
            unpack(FileInputStream(file), format, serializer())
    }
}
