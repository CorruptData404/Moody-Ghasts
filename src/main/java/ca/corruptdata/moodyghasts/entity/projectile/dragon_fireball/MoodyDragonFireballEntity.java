package ca.corruptdata.moodyghasts.entity.projectile.dragon_fireball;

import java.util.List;

import ca.corruptdata.moodyghasts.entity.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.PowerParticleOption;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.ProjectileDeflection;
import net.minecraft.world.entity.projectile.hurtingprojectile.DragonFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

public class MoodyDragonFireballEntity extends DragonFireball {

    private final float splashRange;      // used for the search AABB
    private final float cloudStartRadius;
    private final float cloudEndRadius;
    private final int cloudDuration;
    private final int damageAmplifier;
    private int noDeflectTicks = 5;

    public MoodyDragonFireballEntity(EntityType<? extends MoodyDragonFireballEntity> type, Level level) {
        super(type, level);
        this.splashRange = 4.0F;
        this.cloudStartRadius = 3.0F;
        this.cloudEndRadius = 7.0F;
        this.cloudDuration = 600;
        this.damageAmplifier = 1;
    }

    public MoodyDragonFireballEntity(Level level, LivingEntity owner, float splashRange,
                                     float cloudStartRadius, float cloudEndRadius,
                                     int cloudDuration, int damageAmplifier) {
        super(ModEntities.MOODY_DRAGON_FIREBALL.get(), level);
        this.setOwner(owner);

        this.splashRange = splashRange;
        this.cloudStartRadius = cloudStartRadius;
        this.cloudEndRadius = cloudEndRadius;
        this.cloudDuration = cloudDuration;
        this.damageAmplifier = damageAmplifier;
    }

    @Override
    protected void onHit(HitResult hitResult) {
        // replacement of DragonFireball's onHit, not an extension of it.
        if (hitResult.getType() != HitResult.Type.ENTITY || !this.ownedBy(((EntityHitResult) hitResult).getEntity())) {
            if (!this.level().isClientSide()) {
                double searchRange = this.splashRange;
                List<LivingEntity> entitiesOfClass = this.level().getEntitiesOfClass(
                        LivingEntity.class,
                        this.getBoundingBox().inflate(searchRange, searchRange / 2.0, searchRange)
                );

                AreaEffectCloud cloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
                if (this.getOwner() instanceof LivingEntity livingEntity) {
                    cloud.setOwner(livingEntity);
                }

                cloud.setCustomParticle(PowerParticleOption.create(ParticleTypes.DRAGON_BREATH, 1.0F));
                cloud.setRadius(cloudStartRadius);
                cloud.setDuration(cloudDuration);
                cloud.setRadiusPerTick((cloudEndRadius - cloud.getRadius()) / cloud.getDuration());
                cloud.setPotionDurationScale(0.25F);
                cloud.addEffect(new MobEffectInstance(MobEffects.INSTANT_DAMAGE, 1, damageAmplifier));

                if (!entitiesOfClass.isEmpty()) {
                    double rangeSq = searchRange * searchRange;
                    for (LivingEntity entity : entitiesOfClass) {
                        double dist = this.distanceToSqr(entity);
                        if (dist < rangeSq) {
                            cloud.setPos(entity.getX(), entity.getY(), entity.getZ());
                            break;
                        }
                    }
                }

                this.level().levelEvent(2006, this.blockPosition(), this.isSilent() ? -1 : 1);
                this.level().addFreshEntity(cloud);
                this.discard();
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (this.noDeflectTicks > 0) {
            this.noDeflectTicks--;
        }
    }

    @Override
    public boolean deflect(ProjectileDeflection deflection, @Nullable Entity deflectingEntity, @Nullable EntityReference<Entity> newOwner, boolean byAttack) {
        return this.noDeflectTicks > 0 ? false : super.deflect(deflection, deflectingEntity, newOwner, byAttack);
    }
}