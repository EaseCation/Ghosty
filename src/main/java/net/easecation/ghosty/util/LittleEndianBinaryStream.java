package net.easecation.ghosty.util;

import cn.nukkit.utils.BinaryStream;

public class LittleEndianBinaryStream extends BinaryStream {
    public LittleEndianBinaryStream() {
        super();
    }

    public LittleEndianBinaryStream(BinaryStream source) {
        super(source.getBufferUnsafe(), source.offset);
        setCount(source.getCount());
    }

    @Override
    public int getShort() {
        return getLShort();
    }

    @Override
    public void putShort(int s) {
        putLShort(s);
    }

    @Override
    public int getTriad() {
        return getLTriad();
    }

    @Override
    public void putTriad(int triad) {
        putLTriad(triad);
    }

    @Override
    public int getInt() {
        return getLInt();
    }

    @Override
    public void putInt(int i) {
        putLInt(i);
    }

    @Override
    public long getLong() {
        return getLLong();
    }

    @Override
    public void putLong(long l) {
        putLLong(l);
    }

    @Override
    public float getFloat() {
        return getLFloat();
    }

    @Override
    public void putFloat(float v) {
        putLFloat(v);
    }

    @Override
    public double getDouble() {
        return getLDouble();
    }

    @Override
    public void putDouble(double v) {
        putLDouble(v);
    }

    @Override
    public int getVarInt() {
        return getLInt();
    }

    @Override
    public void putVarInt(int v) {
        putLInt(v);
    }

    @Override
    public long getUnsignedVarInt() {
        return getLInt();
    }

    @Override
    public void putUnsignedVarInt(long v) {
        putLInt((int) v);
    }

    @Override
    public long getVarLong() {
        return getLLong();
    }

    @Override
    public void putVarLong(long v) {
        putLLong(v);
    }

    @Override
    public long getUnsignedVarLong() {
        return getLLong();
    }

    @Override
    public void putUnsignedVarLong(long v) {
        putLLong(v);
    }
}
