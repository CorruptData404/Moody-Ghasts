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
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        float cloudRadius = projConfig.getScaled("cloudRadius", mood);
        int cloudDuration = projConfig.getScaledInt("cloudDuration", mood);
        int regenAmplifier = projConfig.getScaledInt("regenAmplifier", mood);
    public Projectile buildProjectile(Level level, Player owner,
                                      ItemPropertyMap.MoodContext moodContext, ItemPropertyMap.ProjectileConfig projConfig) {
        float cloudRadius = projConfig.getScaled("cloudRadius", moodContext);
        int cloudDuration = projConfig.getScaledInt("cloudDuration", moodContext);
        int regenAmplifier = projConfig.getScaledInt("regenAmplifier", moodContext);

        return new TearEntity(level, owner, Vec3.ZERO, cloudRadius, cloudDuration, regenAmplifier);
    }

    @Override
    public SoundEvent getSoundEvent() {
        //TODO: revisit
        return SoundEvents.GENERIC_SPLASH;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("cloudRadius", "cloudDuration", "regenAmplifier");
    }
}