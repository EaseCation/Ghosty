package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.utils.BinaryStream;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedCustom implements LevelUpdated {

    private final static ThreadLocal<Gson> GSON = ThreadLocal.withInitial(Gson::new);

    private JsonObject obj;

    private LevelUpdatedCustom(JsonObject obj) {
        this.obj = obj;
    }

    public LevelUpdatedCustom(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedCustom of(JsonObject obj) {
        return new LevelUpdatedCustom(obj);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_CUSTOM_EVENT;
    }

    public JsonObject getObj() {
        return obj;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    public void backwardTo(LevelRecordNode node) {
        // 不需要做任何事
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(obj.toString());
    }

    @Override
    public void read(BinaryStream stream) {
        this.obj = GSON.get().fromJson(stream.getString(), JsonObject.class);
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedCustom o)) return false;
        return this.obj.equals(o.obj);
    }

    @Override
    public String toString() {
        return "LevelUpdatedCustom{" +
            "obj=" + obj +
            '}';
    }

    @Override
    public int hashCode() {
        return this.obj.hashCode();
    }
}
