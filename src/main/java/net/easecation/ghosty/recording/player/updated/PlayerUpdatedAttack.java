package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Player;
import cn.nukkit.math.Mth;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.TextFormat;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.playback.PlayerPlaybackEngine;
import net.easecation.ghosty.recording.player.PlayerRecordNode;

public class PlayerUpdatedAttack implements PlayerUpdated {

    private long attackTarget;

    public static PlayerUpdatedAttack of(long attackTarget) {
        return new PlayerUpdatedAttack(attackTarget);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ATTACK;
    }

    @Override
    public boolean hasStates() {
        return false;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        PlayerPlaybackEngine engine = ghost.getEngine();
        if (engine != null && engine.displayAttackDistance) {
            // 从世界中找到目标实体
            ghost.getLevel().getActors().values().stream()
                .filter(e -> e instanceof PlaybackNPC)
                .filter(e -> ((PlaybackNPC) e).getOriginEntityId() == attackTarget)
                .findFirst()
                .ifPresent(e -> {
                    // 计算距离
                    double distance = e.distance(ghost);
                    String distanceStr = getDistanceString(distance);
                    // 显示距离（包含双方的Ping数据）
                    String msg = "[Attack] " + distanceStr + TextFormat.WHITE + " "
                        + "[" + PlayerUpdatedPing.getDisplayPing(ghost.lastPing) + TextFormat.WHITE + "]" + ghost.getAliasName()
                        + TextFormat.RESET + TextFormat.WHITE + " -> "
                        + "[" + PlayerUpdatedPing.getDisplayPing(((PlaybackNPC) e).lastPing) + TextFormat.WHITE + "]" + ((PlaybackNPC) e).getAliasName();
                    for (Player viewer : ghost.getViewers().values()) {
                        viewer.sendMessage(msg);
                    }
                });
        }
    }

    public static String getDistanceString(double distance) {
        if (distance >= 3.6) {
            return TextFormat.RED.toString() + Mth.round(distance, 4);
        } else if (distance >= 3) {
            return TextFormat.YELLOW.toString() + Mth.round(distance, 4);
        } else {
            return TextFormat.GREEN.toString() + Mth.round(distance, 4);
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        // 不需要在init时应用
        return node;
    }

    public PlayerUpdatedAttack(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedAttack(long attackTarget) {
        this.attackTarget = attackTarget;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedAttack o)) return false;
        return attackTarget == o.attackTarget;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putEntityRuntimeId(this.attackTarget);
    }

    @Override
    public void read(BinaryStream stream) {
        this.attackTarget = stream.getEntityRuntimeId();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedAttack{" +
                "attackTarget=" + attackTarget +
                '}';
    }
}
