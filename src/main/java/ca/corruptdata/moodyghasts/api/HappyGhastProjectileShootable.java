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
    int getCooldown();
    boolean tryShootFromMount(Player player, HappyGhast happyGhast);
    InteractionResult onNonMountedUse(Level level, Player player, InteractionHand hand, ItemStack itemstack);
    
    default void applySharedCooldown(Player player) {
        int cooldown = getCooldown();
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
            if (tryShootFromMount(player, happyGhast)) {
                applySharedCooldown(player);
                itemstack.consume(1, player);
                player.awardStat(Stats.ITEM_USED.get((Item)this));
                return InteractionResult.SUCCESS;
            }
            return InteractionResult.PASS;
        }
        
        return onNonMountedUse(level, player, hand, itemstack);
    }
}