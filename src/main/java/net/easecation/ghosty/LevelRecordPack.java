package net.easecation.ghosty;

import cn.nukkit.level.Level;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.easecation.ghosty.playback.LevelPlaybackEngine;
import net.easecation.ghosty.recording.entity.EntityRecord;
import net.easecation.ghosty.recording.level.LevelRecord;
import net.easecation.ghosty.recording.player.PlayerRecord;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class LevelRecordPack {

    private final LevelRecord levelRecord;
    private final List<PlayerRecord> playerRecords;
    private final Long2ObjectMap<EntityRecord> entityRecords;
    private final JsonObject metadata;  // 元数据，不参与任何的回放和录制逻辑，但是可以用于存储一些额外的信息，比如录制的地图名称，录制时间等

    public LevelRecordPack(LevelRecord levelRecord, List<PlayerRecord> playerRecords, Long2ObjectMap<EntityRecord> entityRecords) {
        this(levelRecord, playerRecords, entityRecords, new JsonObject());
    }

    public LevelRecordPack(LevelRecord levelRecord, List<PlayerRecord> playerRecords, Long2ObjectMap<EntityRecord> entityRecords, JsonObject metadata) {
        this.levelRecord = levelRecord;
        this.playerRecords = playerRecords;
        this.entityRecords = entityRecords;
        this.metadata = metadata;
    }

    public JsonObject getMetadata() {
        return metadata;
    }

    public LevelRecord getLevelRecord() {
        return levelRecord;
    }

    public List<PlayerRecord> getPlayerRecords() {
        return playerRecords;
    }

    public Long2ObjectMap<EntityRecord> getEntityRecords() {
        return entityRecords;
    }

    public void pack(OutputStream outputStream) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(outputStream)) {
            zos.setLevel(9);
            // Pack level record
            ZipEntry levelEntry = new ZipEntry("level_record.ecrecl");
            zos.putNextEntry(levelEntry);
            zos.write(levelRecord.toBinary());
            zos.closeEntry();

            // Pack player records
            for (int i = 0; i < playerRecords.size(); i++) {
                PlayerRecord playerRecord = playerRecords.get(i);
                ZipEntry playerEntry = new ZipEntry("player/player_record_" + i + "_" + playerRecord.getPlayerName() + ".ecrecp");
                zos.putNextEntry(playerEntry);
                zos.write(playerRecord.toBinary());
                zos.closeEntry();
            }

            // Pack entity records
            for (Long2ObjectMap.Entry<EntityRecord> entry : entityRecords.long2ObjectEntrySet()) {
                EntityRecord entityRecord = entry.getValue();
                ZipEntry entityEntry = new ZipEntry("entity/entity_record_" + entry.getLongKey() + ".ecrece");
                zos.putNextEntry(entityEntry);
                zos.write(entityRecord.toBinary());
                zos.closeEntry();
            }

            // Metadata
            ZipEntry metadataEntry = new ZipEntry("metadata.json");
            zos.putNextEntry(metadataEntry);
            zos.write(metadata.toString().getBytes());
            zos.closeEntry();
        }
    }

    /**
     * Pack the level record and player records to a zip file. (Blocking)
     * 保存录制的记录到一个zip文件中（阻塞）
     * @param zipFile The zip file to save to. 保存到的zip文件
     * @throws IOException If an I/O error has occurred. 如果发生了IO错误
     */
    public void packFile(File zipFile) throws IOException {
        if (!zipFile.getParentFile().isDirectory()) {
            zipFile.getParentFile().mkdirs();
        }
        this.pack(new FileOutputStream(zipFile));
    }

    public static LevelRecordPack unpack(InputStream inputStream) throws IOException {
        LevelRecord levelRecord = null;
        List<PlayerRecord> playerRecords = new ArrayList<>();
        Long2ObjectMap<EntityRecord> entityRecords = new Long2ObjectOpenHashMap<>();
        JsonObject metadata = null;

        try (ZipInputStream zis = new ZipInputStream(inputStream)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().startsWith("level_record")) {
                    levelRecord = LevelRecord.fromBinary(zis.readAllBytes());
                } else if (entry.getName().startsWith("player/")) {
                    playerRecords.add(PlayerRecord.fromBinary(zis.readAllBytes()));
                } else if (entry.getName().startsWith("entity/")) {
                    EntityRecord entityRecord = EntityRecord.fromBinary(zis.readAllBytes());
                    entityRecords.put(entityRecord.getEntityId(), entityRecord);
                } else if (entry.getName().equals("metadata.json")) {
                    metadata = new Gson().fromJson(new InputStreamReader(zis), JsonObject.class);
                }
                zis.closeEntry();
            }
        }

        if (levelRecord == null) {
            throw new IOException("No level record found in the zip file.");
        }
        if (metadata == null) {
            metadata = new JsonObject();
        }

        return new LevelRecordPack(levelRecord, playerRecords, entityRecords, metadata);
    }

    /**
     * Load a level record pack from a zip file. (Blocking)
     * 从一个zip文件中加载一个LevelRecordPack（阻塞）
     * @param zipFile The zip file to load from. 从中加载的zip文件
     * @return The loaded level record pack. 加载的LevelRecordPack
     * @throws IOException If an I/O error has occurred. 如果发生了IO错误
     */
    public static LevelRecordPack unpackFile(File zipFile) throws IOException {
        return unpack(new FileInputStream(zipFile));
    }

    public LevelPlaybackEngine createPlayback(Level level) {
        return new LevelPlaybackEngine(levelRecord, level, playerRecords, entityRecords);
    }

}
