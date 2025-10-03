package ca.corruptdata.moodyghasts.item.custom;

import net.minecraft.world.entity.LivingEntity;
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

        if (!level.isClientSide) {
            int random = level.random.nextInt(2);
            if(random == 0) livingEntity.setRemainingFireTicks(100);
            else if(random == 1) livingEntity.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                    net.minecraft.world.effect.MobEffects.FIRE_RESISTANCE, 600, 0));
        }

        return resultStack;
    }
}