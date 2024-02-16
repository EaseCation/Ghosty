package net.easecation.ghosty.util;

import cn.nukkit.block.Block;
import cn.nukkit.entity.data.Skin;
import cn.nukkit.item.Item;
import cn.nukkit.item.Items;
import cn.nukkit.nbt.NBTIO;
import cn.nukkit.nbt.tag.CompoundTag;
import cn.nukkit.utils.BinaryStream;

import java.io.IOException;
import java.nio.ByteOrder;

public final class PersistenceBinaryStreamHelper {
    public static Item getItem(BinaryStream stream) {
        byte[] bytes = stream.getByteArray();
        CompoundTag nbt;
        try {
            nbt = NBTIO.read(bytes, ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Item item = NBTIO.getItemHelper(nbt);
        if (item.getClass() == Item.class) {
            // unknown item
            return Items.air();
        }
        return item;
    }

    public static void putItem(BinaryStream stream, Item block) {
        CompoundTag nbt = NBTIO.putItemHelper(block);
        byte[] bytes;
        try {
            bytes = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream.putByteArray(bytes);
    }

    public static Block getBlock(BinaryStream stream) {
        byte[] bytes = stream.getByteArray();
        CompoundTag nbt;
        try {
            nbt = NBTIO.read(bytes, ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return NBTIO.getBlockHelper(nbt);
    }

    public static void putBlock(BinaryStream stream, Block block) {
        CompoundTag nbt = NBTIO.putBlockHelper(block);
        byte[] bytes;
        try {
            bytes = NBTIO.write(nbt, ByteOrder.LITTLE_ENDIAN, false);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        stream.putByteArray(bytes);
    }

    public static Skin getSkin(BinaryStream stream) {
        Skin skin = new Skin();
        skin.setSkinId(stream.getString());
        skin.setSkinData(stream.getByteArray());
        skin.setCapeData(stream.getByteArray());
        skin.setGeometryName(stream.getString());
        skin.setGeometryData(stream.getString());
        return skin;
    }

    public static void putSkin(BinaryStream stream, Skin skin) {
        stream.putString(skin.getSkinId());
        if (skin.isValid()) {
            stream.putByteArray(skin.getSkinData().data);
        } else {
            stream.putByteArray(Skin.FULL_WHITE_SKIN);
        }
        stream.putByteArray(skin.getCapeData().data);
        stream.putString(skin.getGeometryName());
        stream.putString(skin.getGeometryData());
    }

    private PersistenceBinaryStreamHelper() {
    }
}
