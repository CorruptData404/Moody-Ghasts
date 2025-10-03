package ca.corruptdata.moodyghasts.item.custom;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class FrostedCookieItem extends Item {
    public FrostedCookieItem(Properties properties) {
        super(properties.food(new FoodProperties.Builder()
                .nutrition(2)           // Same as a regular cookie
                .build()));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack resultStack = super.finishUsingItem(stack, level, livingEntity);

        if (!level.isClientSide) {
            int random = level.random.nextInt(2);
            if(random == 0) livingEntity.setTicksFrozen(500);
            else if(random == 1) livingEntity.addEffect(new MobEffectInstance(
                    MobEffects.RESISTANCE, 600, 1));
        }
        
        return resultStack;
    }
}