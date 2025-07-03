package ca.corruptdata.moodyghasts.item;

import ca.corruptdata.moodyghasts.api.HappyGhastProjectileShootable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

public abstract class HappyGhastProjectileItem extends Item implements HappyGhastProjectileShootable {
    
    private final int cooldown;

    public HappyGhastProjectileItem(Properties properties, int cooldown) {
        super(properties);
        this.cooldown = cooldown;
    }

    @Override
    public int moodyghasts$getCooldown() {
        return cooldown;
    }

    @Override
    public @NotNull InteractionResult use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        return HappyGhastProjectileShootable.super.use(level, player, hand);
    }
}