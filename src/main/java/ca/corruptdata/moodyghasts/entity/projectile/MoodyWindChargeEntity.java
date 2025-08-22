package ca.corruptdata.moodyghasts.entity.projectile;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.windcharge.AbstractWindCharge;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SimpleExplosionDamageCalculator;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;
import java.util.function.Function;

public class MoodyWindChargeEntity extends AbstractWindCharge {
    private static final double MIN_CAMERA_DISTANCE_SQUARED = 12.25;
    private final float customRadius;
    private final ExplosionDamageCalculator explosionDamageCalculator;

    public MoodyWindChargeEntity(EntityType<? extends AbstractWindCharge> entityType, Level level) {
        super(entityType, level);
        this.customRadius = 1.2F;
        this.explosionDamageCalculator = new SimpleExplosionDamageCalculator(
                true, false,
                Optional.of(1.22F),
                BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
        );
    }

    public MoodyWindChargeEntity(Level level, LivingEntity owner, Vec3 movement, float radius, float strength) {
        super(ModEntities.MOODY_WIND_CHARGE.get(), level);
        this.setOwner(owner);
        this.setDeltaMovement(movement);
        this.customRadius = radius;
        this.explosionDamageCalculator = new SimpleExplosionDamageCalculator(
                true, false,
                Optional.of(1.22F * strength),
                BuiltInRegistries.BLOCK.get(BlockTags.BLOCKS_WIND_CHARGE_EXPLOSIONS).map(Function.identity())
        );
    }


    @Override
    protected void explode(Vec3 position) {

        this.level().explode(
            this,
            null,
            explosionDamageCalculator,
            position.x(),
            position.y(),
            position.z(),
            this.customRadius,
            false,
            Level.ExplosionInteraction.TRIGGER,
            ParticleTypes.GUST_EMITTER_SMALL,
            ParticleTypes.GUST_EMITTER_LARGE,
            SoundEvents.WIND_CHARGE_BURST
        );
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return this.tickCount < 2 && distance < MIN_CAMERA_DISTANCE_SQUARED ? false : super.shouldRenderAtSqrDistance(distance);
    }

}