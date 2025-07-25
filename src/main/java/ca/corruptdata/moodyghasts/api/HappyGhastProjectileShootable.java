package ca.corruptdata.moodyghasts.api;

import ca.corruptdata.moodyghasts.util.ModTags;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public interface HappyGhastProjectileShootable {
    float projectilePower = 0;
    int moodyghasts$getCooldown();
    boolean moodyghasts$tryShootFromGhast(Player player, HappyGhast happyGhast);

    default InteractionResult moodyghasts$onNonMountedUse(Level level, Player player, InteractionHand hand, ItemStack itemstack){
        return null;
    }
    
    default void applySharedCooldown(Player player) {
        int cooldown = moodyghasts$getCooldown();
        ItemCooldowns cooldownTracker = player.getCooldowns();

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && stack.is(ModTags.Items.HAPPY_GHAST_PROJECTILES)) {
                cooldownTracker.addCooldown(stack, cooldown);
            }
        }
    }
    
    default InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (player.getVehicle() instanceof HappyGhast happyGhast && happyGhast.getControllingPassenger() == player) {
            if (moodyghasts$tryShootFromGhast(player, happyGhast)) {
                applySharedCooldown(player);
                itemstack.consume(1, player);
                player.awardStat(Stats.ITEM_USED.get((Item)this));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        return moodyghasts$onNonMountedUse(level, player, hand, itemstack);
    }
}