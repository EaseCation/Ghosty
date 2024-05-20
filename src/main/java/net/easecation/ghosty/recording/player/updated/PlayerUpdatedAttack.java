package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.utils.BinaryStream;
import net.easecation.ghosty.entity.PlaybackNPC;
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
        // 空
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
