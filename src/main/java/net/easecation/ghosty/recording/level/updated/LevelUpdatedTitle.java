package net.easecation.ghosty.recording.level.updated;

import cn.nukkit.Player;
import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.recording.level.LevelRecordNode;

public class LevelUpdatedTitle implements LevelUpdated {

    private String title;
    private String subTitle;
    int fadeInTime;
    int stayTime;
    int fadeOutTime;

    private LevelUpdatedTitle(String title, String subTitle, int fadeInTime, int stayTime, int fadeOutTime) {
        this.title = title;
        this.subTitle = subTitle;
        this.fadeInTime = fadeInTime;
        this.stayTime = stayTime;
        this.fadeOutTime = fadeOutTime;
    }

    public LevelUpdatedTitle(BinaryStream stream) {
        this.read(stream);
    }

    public static LevelUpdatedTitle of(String title, String subTitle, int fadeInTime, int stayTime, int fadeOutTime) {
        return new LevelUpdatedTitle(title, subTitle, fadeInTime, stayTime, fadeOutTime);
    }

    @Override
    public int getUpdateTypeId() {
        return LevelUpdated.TYPE_TITLE;
    }

    @Override
    public void processTo(LevelRecordNode node) {
        node.offerLevelGlobalCallback((level) -> {
            for (Player player : level.getPlayers().values()) {
                player.sendTitle(this.title, this.subTitle, this.fadeInTime, this.stayTime, this.fadeOutTime);
            }
        });
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putString(title);
        stream.putString(subTitle);
        stream.putLInt(fadeInTime);
        stream.putLInt(stayTime);
        stream.putLInt(fadeOutTime);
    }

    @Override
    public void read(BinaryStream stream) {
        this.title = stream.getString();
        this.subTitle = stream.getString();
        this.fadeInTime = stream.getLInt();
        this.stayTime = stream.getLInt();
        this.fadeOutTime = stream.getLInt();
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof LevelUpdatedTitle o)) return false;
        return this.title.equals(o.title) && this.subTitle.equals(o.subTitle) && this.fadeInTime == o.fadeInTime && this.fadeOutTime == o.fadeOutTime && this.stayTime == o.stayTime;
    }

    @Override
    public String toString() {
        return "LevelUpdatedTitle{" +
            "title='" + title + '\'' +
            ", subTitle='" + subTitle + '\'' +
            ", fadeInTime=" + fadeInTime +
            ", stayTime=" + stayTime +
            ", fadeOutTime=" + fadeOutTime +
            '}';
    }

    @Override
    public int hashCode() {
        return this.title.hashCode() ^ this.subTitle.hashCode() ^ this.fadeInTime ^ this.fadeOutTime ^ this.stayTime;
    }
}
