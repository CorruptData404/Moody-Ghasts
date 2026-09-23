package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.entity.projectile.tear.TearEntity;
import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class TearFactory implements ProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner,
                                      ItemPropertyMap.MoodContext moodContext, ItemPropertyMap.ProjectileConfig projConfig) {
        float cloudRadius = projConfig.getScaled("cloudRadius", moodContext);
        int cloudDuration = projConfig.getScaledInt("cloudDuration", moodContext);
        int effectDuration = projConfig.getScaledInt("effectDuration", moodContext);
        int regenAmplifier = projConfig.getScaledInt("regenAmplifier", moodContext);

        return new TearEntity(level, owner, Vec3.ZERO,
                cloudRadius, cloudDuration, effectDuration, regenAmplifier);
    }

    @Override
    public SoundEvent getSoundEvent() {
        //TODO: Custom Sound
        return SoundEvents.POINTED_DRIPSTONE_DRIP_WATER_INTO_CAULDRON;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("cloudRadius", "cloudDuration", "effectDuration", "regenAmplifier");
    }
}