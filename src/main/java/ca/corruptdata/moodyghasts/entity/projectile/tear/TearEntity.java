package ca.corruptdata.moodyghasts.entity.projectile.tear;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class TearEntity extends ThrowableItemProjectile {

    private final float directHitHeal;
    private final float cloudRadius;
    private final int cloudDuration;
    private final int effectDuration;
    private final int regenAmplifier;

    public TearEntity(EntityType<? extends TearEntity> type, Level level) {
        super(type, level);
        this.directHitHeal = 2.0F;
        this.cloudRadius = 1.5F;
        this.cloudDuration = 140;
        this.effectDuration = 120;
        this.regenAmplifier = 1;
    }

    public TearEntity(Level level, LivingEntity owner,float directHitHeal, float cloudRadius,
                      int cloudDuration, int effectDuration, int regenAmplifier) {
        super(ModEntities.MOODY_TEAR.get(), owner, level, Items.GHAST_TEAR.getDefaultInstance());
        this.directHitHeal = directHitHeal;
        this.cloudRadius = cloudRadius;
        this.cloudDuration = cloudDuration;
        this.effectDuration = effectDuration;
        this.regenAmplifier = regenAmplifier;
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);
        if (this.level().isClientSide()) return;

        Entity hitEntity = hitResult.getEntity();
        if (hitEntity instanceof LivingEntity livingEntity) {
            if (livingEntity.isInvertedHealAndHarm()) {
                livingEntity.hurt(this.damageSources().magic(), directHitHeal);
            } else {
                livingEntity.heal(directHitHeal);
            }
        }

        spawnCloud(hitEntity.getX(), hitEntity.getY(), hitEntity.getZ());
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (this.level().isClientSide()) return;

        spawnCloud(this.getX(), this.getY(), this.getZ());
        this.discard();
    }

    private void spawnCloud(double x, double y, double z) {
        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), x, y, z);
        if (this.getOwner() instanceof LivingEntity livingEntity) {
            cloud.setOwner(livingEntity);
        }

        cloud.setRadius(cloudRadius);
        cloud.setDuration(cloudDuration);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectDuration, regenAmplifier));

        this.level().addFreshEntity(cloud);
    }

    @Override
    protected Item getDefaultItem() {
        return Items.GHAST_TEAR;
    }
}