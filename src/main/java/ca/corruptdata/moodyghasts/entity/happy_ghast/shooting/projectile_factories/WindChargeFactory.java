package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.entity.projectile.MoodyWindChargeEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WindChargeFactory implements GhastProjectileFactory {
    @Override
    public Projectile createProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        float radius = projConfig.getRadius(mood);
        float strength = projConfig.getStrength(mood);
        return new MoodyWindChargeEntity(level, owner, Vec3.ZERO, radius, strength);
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.WIND_CHARGE_THROW;
    }
}
