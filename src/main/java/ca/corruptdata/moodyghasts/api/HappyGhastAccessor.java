package ca.corruptdata.moodyghasts.api;

import net.minecraft.world.entity.player.Player;

public interface HappyGhastAccessor {
    boolean moodyghasts$beginShooting(Player player);
    boolean moodyghasts$isShooting();
    float moodyghasts$getMood();
}