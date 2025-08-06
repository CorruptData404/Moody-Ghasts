package ca.corruptdata.moodyghasts.api;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface HappyGhastAccessor {
    boolean moodyghasts$beginShooting(Player player, HappyGhastProjectileShootable projectile, float basePower);
    boolean moodyghasts$isShooting();
    float moodyghasts$getMood();
    void moodyghasts$adjustMood(float moodDelta);
    InteractionResult moodyghasts$feed(ItemStack treat);
}