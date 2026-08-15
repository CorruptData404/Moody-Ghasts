package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.entity.projectile.wind_charge.MoodyWindChargeEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class WindChargeFactory implements ProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        return new MoodyWindChargeEntity(level, owner, Vec3.ZERO, projConfig.getRadius(mood), projConfig.getStrength(mood));
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.WIND_CHARGE_THROW;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("radius", "strength");
    }
}
