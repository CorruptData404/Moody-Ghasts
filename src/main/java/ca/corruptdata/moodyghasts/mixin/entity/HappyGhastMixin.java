package ca.corruptdata.moodyghasts.mixin.entity;

import ca.corruptdata.moodyghasts.util.MoodThresholdsManager;
import ca.corruptdata.moodyghasts.api.HappyGhastAccessor;
import ca.corruptdata.moodyghasts.api.HappyGhastProjectileShootable;
import ca.corruptdata.moodyghasts.entity.projectile.GhastIceChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(HappyGhast.class)
public class HappyGhastMixin implements HappyGhastAccessor {
    
    @Unique
    private static final float INITIAL_MOOD = 40F;
    @Unique
    private static final float MIN_MOOD = 0.0f;
    @Unique
    private static final float MAX_MOOD = 100.0f;
    @Unique
    private int moodyghasts$shootChargeTime = 0;
    @Unique
    private static final EntityDataAccessor<Float> MOOD =
            SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.FLOAT);
    @Unique
    private static final EntityDataAccessor<Boolean> IS_SHOOTING =
            SynchedEntityData.defineId(HappyGhast.class, EntityDataSerializers.BOOLEAN);
    @Unique
    private HappyGhastProjectileShootable moodyghasts$currentProjectileItem;
    @Unique
    private Player moodyghasts$shootingPlayer;
    @Unique
    private float moodyghasts$baseVelocity;

    @Inject(method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", at = @At("TAIL"))
    private void defineCustomData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_SHOOTING, false);
        builder.define(MOOD, INITIAL_MOOD);
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void onTick(CallbackInfo ci) {
        HappyGhast ghast = (HappyGhast)(Object)this;

        if(!ghast.level().isClientSide() && moodyghasts$isShooting()){
            if(ghast.getControllingPassenger() == moodyghasts$shootingPlayer && !(moodyghasts$shootChargeTime > 20)){
                moodyghasts$shootChargeTime++;
            }
            else
            {
                moodyghasts$setShooting(false);
                moodyghasts$shootChargeTime = 0;
                moodyghasts$shootingPlayer = null;
                moodyghasts$currentProjectileItem = null;
            }
        }
    }

    @Inject(method = "tickRidden", at = @At("TAIL"))
    private void onTickRidden(Player player, Vec3 vec3, CallbackInfo ci) {
        if (!moodyghasts$isShooting()) return;

        HappyGhast ghast = (HappyGhast)(Object)this;

        if (moodyghasts$shootChargeTime == 10) {
            ghast.level().levelEvent(null, 1015, ghast.blockPosition(), 0);
        }

        if (moodyghasts$shootChargeTime == 20) {
                if (moodyghasts$currentProjectileItem instanceof IceChargeItem) {
                    moodyghasts$shootCharge(player, ghast,
                        new GhastIceChargeEntity(player, ghast.level(), moodyghasts$getMood()),
                            moodyghasts$baseVelocity);
                }
            // TODO: Implement other projectile types
            moodyghasts$setShooting(false);
            moodyghasts$shootChargeTime = 0;
            moodyghasts$shootingPlayer = null;
            moodyghasts$currentProjectileItem = null;
        }
    }

    @Unique
    public boolean moodyghasts$beginShooting(Player player, HappyGhastProjectileShootable projectileItem, float baseVelocity) {
        HappyGhast ghast = (HappyGhast)(Object)this;

        if (!ghast.level().isClientSide) {
            if (moodyghasts$shootChargeTime == 0) { // Only start if not already charging
                moodyghasts$setShooting(true);
                moodyghasts$shootChargeTime = 1; // Start charging
                moodyghasts$shootingPlayer = player;
                moodyghasts$currentProjectileItem = projectileItem;
                moodyghasts$baseVelocity = baseVelocity;
                return true;
            }
        }
        return false;
    }

    @Unique
    private void moodyghasts$shootCharge(Player player, HappyGhast ghast, AbstractHurtingProjectile projectile, float baseVelocity) {
        if (!(ghast.level() instanceof ServerLevel serverLevel)) return;

        ghast.level().levelEvent(null, 1016, ghast.blockPosition(), 0);

        // Clamp vertical rotation (pitch) to ±60° to avoid extreme angles
        float clampedPitch = Mth.clamp(player.getXRot() - 4, -60f, 60f);

        // Use clamped pitch and yaw to get look direction
        Vec3 dir = Vec3.directionFromRotation(clampedPitch, player.getYRot());

        // Start 4 blocks forward from the ghast's eye in all directions
        Vec3 spawn = new Vec3(
                ghast.getX() + dir.x * 4.0,
                ghast.getEyeY() + dir.y * 4.0,
                ghast.getZ() + dir.z * 4.0
        );
        projectile.setPos(spawn);

        // Shoot projectile in the desired direction with specified power
        projectile.shoot(dir.x, dir.y, dir.z, baseVelocity, 0f);

        serverLevel.addFreshEntity(projectile);
    }

    @Unique
    private void moodyghasts$addParticlesAroundSelf(ParticleOptions particleOption)
    {
        HappyGhast ghast = (HappyGhast)(Object)this;
        for (int i = 0; i < 10; i++) {
            double d0 = ghast.getRandom().nextGaussian() * 0.02;
            double d1 = ghast.getRandom().nextGaussian() * 0.02;
            double d2 = ghast.getRandom().nextGaussian() * 0.02;
            ghast.level().addParticle(particleOption, ghast.getRandomX(1.0), ghast.getRandomY() + 1.0, ghast.getRandomZ(1.0), d0, d1, d2);
        }
    }


    @Unique
    public boolean moodyghasts$wouldCrossMoodThreshold(float delta) {
        float currentMood = moodyghasts$getMood();
        float newMood = Mth.clamp(currentMood + delta, MIN_MOOD, MAX_MOOD);

        Map<String, Float> thresholds = MoodThresholdsManager.getCurrentInstance().getMoodMap();

        // Check if the mood change would cross any of the threshold boundaries
        return thresholds.values().stream()
                .anyMatch(threshold ->
                        (currentMood <= threshold && newMood > threshold) ||
                                (currentMood > threshold && newMood <= threshold));
    }



    @Unique
    public boolean moodyghasts$isShooting() {
        HappyGhast ghast = (HappyGhast)(Object)this;
        return ghast.getEntityData().get(IS_SHOOTING);
    }

    @Unique
    public float moodyghasts$getMood() {
        HappyGhast ghast = (HappyGhast)(Object)this;
        return ghast.getEntityData().get(MOOD);
    }

    @Unique
    private void moodyghasts$setMood(float mood) {
        moodyghasts$adjustMood(mood - moodyghasts$getMood());
    }

    /**
     * Adjusts the mood of the Happy Ghast by the given delta.
     * Mood ranges from 0 (happiest) to 100 (angriest).
     * Will show particles when crossing mood thresholds.
     * @param delta The amount to adjust the mood by (positive = angrier, negative = happier)
     */
    @Unique
    public void moodyghasts$adjustMood(float delta) {

        if (moodyghasts$wouldCrossMoodThreshold(delta)) {
            moodyghasts$addParticlesAroundSelf(delta > 0 ?
                    ParticleTypes.ANGRY_VILLAGER :
                    ParticleTypes.HAPPY_VILLAGER);
        }

        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(MOOD, Mth.clamp(moodyghasts$getMood() + delta, MIN_MOOD, MAX_MOOD));
    }

    @Unique
    private void moodyghasts$setShooting(boolean shooting) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(IS_SHOOTING, shooting);
    }
}