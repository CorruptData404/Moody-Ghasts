package ca.corruptdata.moodyghasts.item.custom;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class SpicyCookieItem extends Item {
    public SpicyCookieItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .nutrition(2)           // Same as a regular cookie
                .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide && livingEntity instanceof Player player) {
            //TODO: custom consumption effects
        }

        return resultStack;
    }
}