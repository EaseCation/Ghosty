package net.easecation.ghosty.playback

import cn.nukkit.math.Mth
import cn.nukkit.math.Vector3

object AttackDistanceCalculator {

    private const val DEFAULT_PLAYER_WIDTH = 0.6
    private const val DEFAULT_PLAYER_RADIUS = DEFAULT_PLAYER_WIDTH / 2.0
    private const val DEFAULT_PLAYER_HEIGHT = 1.8
    private const val DEFAULT_PLAYER_EYE_HEIGHT = DEFAULT_PLAYER_HEIGHT * 0.9

    @JvmStatic
    fun calculate(attackerFeet: Vector3, targetFeet: Vector3): Double {
        val attackerEyeY = attackerFeet.y + DEFAULT_PLAYER_EYE_HEIGHT
        val targetClosestY = Mth.clamp(attackerEyeY, targetFeet.y, targetFeet.y + DEFAULT_PLAYER_HEIGHT)
        val distanceToCenterLine = Mth.length(
            attackerFeet.x - targetFeet.x,
            attackerEyeY - targetClosestY,
            attackerFeet.z - targetFeet.z,
        )
        return (distanceToCenterLine - DEFAULT_PLAYER_RADIUS).coerceAtLeast(0.0)
    }
}
