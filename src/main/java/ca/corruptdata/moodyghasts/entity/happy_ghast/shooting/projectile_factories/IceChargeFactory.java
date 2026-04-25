package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.projectile.MoodyIceChargeEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

public class IceChargeFactory implements GhastProjectileFactory {

    protected static final Logger LOGGER = MoodyGhasts.LOGGER;

    @Override
    public Projectile createProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        float radius = projConfig.getRadius(mood);
        float strength = projConfig.getStrength(mood);
        LOGGER.info("Creating Ice Charge with radius {} and strength {}", radius, strength);
        return new MoodyIceChargeEntity(level, owner, Vec3.ZERO, radius, strength);
    }

    @Override
    public SoundEvent getSoundEvent() {
        //TODO: Custom Sound
        return SoundEvents.SNOWBALL_THROW;
    }
}
