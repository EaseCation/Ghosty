package net.easecation.ghosty.recording.player.updated;

import cn.nukkit.Player;
import cn.nukkit.Server;
import cn.nukkit.network.protocol.AnimatePacket;
import cn.nukkit.network.protocol.EntityEventPacket;
import cn.nukkit.utils.BinaryStream;
import cn.nukkit.utils.TextFormat;
import net.easecation.ghosty.entity.PlaybackNPC;
import net.easecation.ghosty.playback.LevelPlaybackEngine;
import net.easecation.ghosty.playback.PlayerPlaybackEngine;
import net.easecation.ghosty.recording.player.PlayerRecordNode;
import net.easecation.ghosty.recording.player.SkinlessPlayerRecord;

public class PlayerUpdatedEntityEvent implements PlayerUpdated {

    private int event;
    private int data;

    public static PlayerUpdatedEntityEvent of(int event, int data) {
        return new PlayerUpdatedEntityEvent(event, data);
    }

    @Override
    public int getUpdateTypeId() {
        return PlayerUpdated.TYPE_ENTITY_EVENT;
    }

    @Override
    public boolean hasStates() {
        return false;
    }

    @Override
    public void processTo(PlaybackNPC ghost) {
        if (ghost != null && ghost.getInventory() != null) {
            EntityEventPacket pk = new EntityEventPacket();
            pk.eid = ghost.getId();
            pk.event = this.event;
            pk.data = this.data;
            Server.broadcastPacket(ghost.getViewers().values(), pk);
            // 攻击距离显示（仅限于旧版本录像，性能堪忧，新版本中将直接通过UpdatedAttack来得到明确的攻击事件）
            // TODO Remove it!
            PlayerPlaybackEngine engine = ghost.getEngine();
            LevelPlaybackEngine levelPlaybackEngine = engine.getLevelPlaybackEngine();
            if (levelPlaybackEngine != null && engine.displayAttackDistance && engine.getRecord() instanceof SkinlessPlayerRecord rec && rec.getFormatVersion() < 3) {
                if (this.event == EntityEventPacket.HURT_ANIMATION) {
                    // 寻找攻击者
                    for (PlayerPlaybackEngine other : levelPlaybackEngine.getPlayerPlaybackEngines()) {
                        if (other == engine) continue;
                        other.getIteratorUnsafe().peekBackwardFirstMatch(u -> u.getUpdateTypeId() == PlayerUpdated.TYPE_ANIMATE).ifPresent(playerUpdated -> {
                            if (playerUpdated.entry() instanceof PlayerUpdatedAnimate updatedAnimate && updatedAnimate.getAction() == AnimatePacket.Action.SWING_ARM.getId()) {
                                // 找到攻击者
                                PlaybackNPC attacker = other.getNPC();
                                if (attacker == null || attacker.getInventory() == null || !attacker.getInventory().getItemInHand().isSword()) {
                                    return;
                                }
                                // 计算距离
                                double distance = attacker.distance(ghost);
                                String distanceStr = PlayerUpdatedAttack.getDistanceString(distance);
                                // 显示距离
                                String msg = "[Attack] " + distanceStr + TextFormat.WHITE + " " + attacker.getNameTag() + TextFormat.RESET + TextFormat.WHITE + " -> " + ghost.getNameTag();
                                for (Player viewer : ghost.getViewers().values()) {
                                    viewer.sendMessage(msg);
                                }
                            }
                        });
                    }
                }
            }
        }
    }

    @Override
    public PlayerRecordNode applyTo(PlayerRecordNode node) {
        // 不需要在init时应用
        return node;
    }

    public PlayerUpdatedEntityEvent(BinaryStream stream) {
        read(stream);
    }

    private PlayerUpdatedEntityEvent(int event, int data) {
        this.event = event;
        this.data = data;
    }

    @Override
    public boolean equals(Object obj) {
        if(!(obj instanceof PlayerUpdatedEntityEvent o)) return false;
        return event == o.event && data == o.data;
    }

    @Override
    public void write(BinaryStream stream) {
        stream.putByte((byte) this.event);
        stream.putVarInt(this.data);
    }

    @Override
    public void read(BinaryStream stream) {
        this.event = stream.getByte();
        this.data = stream.getVarInt();
    }

    @Override
    public String toString() {
        return "PlayerUpdatedEntityEvent{" +
            "event=" + event +
            ", data=" + data +
            '}';
    }
}
