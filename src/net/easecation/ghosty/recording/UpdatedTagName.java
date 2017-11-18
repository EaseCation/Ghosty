package net.easecation.ghosty.recording;

import net.easecation.ghosty.entity.PlaybackNPC;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
class UpdatedTagName implements Updated {

    static UpdatedTagName of(String tn) {
        return new UpdatedTagName(tn);
    }

    private String tn;

    @Override
    public void processTo(PlaybackNPC ghost) {
        ghost.setNameTag(tn);
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setTagName(tn);
        return node;
    }

    private UpdatedTagName(String tn) {
        this.tn = tn;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedTagName)) return false;
        UpdatedTagName o = (UpdatedTagName) obj;
        return (Objects.equals(tn, o.tn));
    }
}
