package ca.corruptdata.moodyghasts.api;

import net.minecraft.world.entity.player.Player;

public interface HappyGhastAccessor {
    boolean moodyghasts$beginShooting(Player player, HappyGhastProjectileShootable projectile, float basePower);
    boolean moodyghasts$isShooting();
    float moodyghasts$getMood();
    void moodyghasts$adjustMood(float moodDelta);
}