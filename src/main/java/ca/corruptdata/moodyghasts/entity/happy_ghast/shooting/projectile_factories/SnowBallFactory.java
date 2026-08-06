package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

public class SnowBallFactory implements GhastProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        Snowball proj = new Snowball(level, 0.0, 0.0, 0.0, new ItemStack(Items.SNOWBALL));

        if(Config.SHOOT_LOGGING.get())
            LOGGER.info("Creating Snowball with no mood scaling");

        return proj;
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.SNOWBALL_THROW;
    }
}
