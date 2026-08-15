package ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories;

import ca.corruptdata.moodyghasts.item.data.ItemPropertyMap;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

public class LargeFireballFactory implements ProjectileFactory {

    @Override
    public Projectile buildProjectile(Level level, Player owner, float mood, ItemPropertyMap.ProjectileConfig projConfig) {
        return new LargeFireball(level, owner, Vec3.ZERO, projConfig.getScaledInt("strength", mood));
    }

    @Override
    public SoundEvent getSoundEvent() {
        return SoundEvents.FIRECHARGE_USE;
    }

    @Override
    public Set<String> getRecognizedMoodScalingKeys() {
        return Set.of("strength");
    }

}
