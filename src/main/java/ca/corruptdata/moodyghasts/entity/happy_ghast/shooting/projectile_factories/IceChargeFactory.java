package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.Config;
import ca.corruptdata.moodyghasts.entity.projectile.ice_charge.MoodyIceChargeEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class IceChargeFactory implements ProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        return new MoodyIceChargeEntity(level, owner, Vec3.ZERO, projConfig.getRadius(mood), projConfig.getStrength(mood));
    }

    @Override
    public SoundEvent getSoundEvent() {
        //TODO: Custom Sound
        return SoundEvents.SNOWBALL_THROW;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("radius", "strength");
    }

}
