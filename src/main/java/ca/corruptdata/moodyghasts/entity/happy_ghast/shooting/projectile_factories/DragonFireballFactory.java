package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.entity.projectile.dragon_fireball.MoodyDragonFireballEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Set;

public class DragonFireballFactory implements ProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        float splashRange = projConfig.getScaled("splashRange", mood);
        float cloudStartRadius = projConfig.getScaled("cloudStartRadius", mood);
        float cloudEndRadius = projConfig.getScaled("cloudEndRadius", mood);
        int cloudDuration = projConfig.getScaledInt("cloudDuration", mood);
        int damageAmplifier = projConfig.getScaledInt("damageAmplifier", mood);

        return new MoodyDragonFireballEntity(level, owner, splashRange,
                cloudStartRadius, cloudEndRadius, cloudDuration, damageAmplifier);
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.ENDER_DRAGON_SHOOT;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("splashRange", "cloudStartRadius", "cloudEndRadius", "cloudDuration", "damageAmplifier");
    }
}