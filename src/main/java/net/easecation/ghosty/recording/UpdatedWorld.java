package net.easecation.ghosty.recording;

import cn.nukkit.Server;
import cn.nukkit.level.Location;
import net.easecation.ghosty.entity.PlaybackNPC;

import java.util.Objects;

/**
 * Created by Mulan Lin('Snake1999') on 2016/11/19 15:23.
 */
class UpdatedWorld implements Updated {

    static UpdatedWorld of(String tn) {
        return new UpdatedWorld(tn);
    }

    private String wn;

    @Override
    public void processTo(PlaybackNPC ghost) {
        Location location = ghost.getLocation();
        location.level = Server.getInstance().getLevelByName(wn);
        if(location.level == null) return;
        ghost.teleport(location);
    }

    @Override
    public RecordNode applyTo(RecordNode node) {
        node.setLevel(wn);
        return node;
    }

    private UpdatedWorld(String wn) {
        this.wn = wn;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof UpdatedWorld)) return false;
        UpdatedWorld o = (UpdatedWorld) obj;
        return (Objects.equals(wn, o.wn));
    }

}
