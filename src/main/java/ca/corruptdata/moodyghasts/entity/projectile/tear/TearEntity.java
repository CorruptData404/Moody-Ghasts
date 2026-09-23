package ca.corruptdata.moodyghasts.entity.projectile.tear;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.hurtingprojectile.AbstractHurtingProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class TearEntity extends AbstractHurtingProjectile implements ItemSupplier {

    private final float cloudRadius;
    private final int cloudDuration;
    private final int effectDuration;
    private final int regenAmplifier;

    // Owner is excluded from hits for a few ticks after spawning
    private int noOwnerHitTicks = 5;

    public TearEntity(EntityType<? extends TearEntity> type, Level level) {
        super(type, level);
        this.cloudRadius = 1.5F;
        this.cloudDuration = 140;
        this.effectDuration = 120;
        this.regenAmplifier = 1;
    }

    public TearEntity(Level level, LivingEntity owner, Vec3 direction,
                      float cloudRadius, int cloudDuration, int effectDuration, int regenAmplifier) {
        super(ModEntities.MOODY_TEAR.get(), owner, direction, level);
        this.cloudRadius = cloudRadius;
        this.cloudDuration = cloudDuration;
        this.effectDuration = effectDuration;
        this.regenAmplifier = regenAmplifier;
    }

    @Override
    public void tick() {
        super.tick();

        if (noOwnerHitTicks > 0) {
            noOwnerHitTicks--;
        }
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (noOwnerHitTicks > 0 && this.ownedBy(target)) return false;
        return super.canHitEntity(target);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide()) return;

        AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
        if (this.getOwner() instanceof LivingEntity livingEntity) {
            cloud.setOwner(livingEntity);
        }

        cloud.setRadius(cloudRadius);
        cloud.setDuration(cloudDuration);
        cloud.setRadiusPerTick(-cloud.getRadius() / cloud.getDuration());
        cloud.addEffect(new MobEffectInstance(MobEffects.REGENERATION, effectDuration, regenAmplifier));

        this.level().addFreshEntity(cloud);
        this.discard();
    }
    @Override
    protected ParticleOptions getTrailParticle() {
        return null;
    }

    @Override
    protected boolean shouldBurn() {
        return false;
    }

    @Override
    public ItemStack getItem() {
        return Items.GHAST_TEAR.getDefaultInstance();
    }
}