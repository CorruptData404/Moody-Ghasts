package ca.corruptdata.moodyghasts.mixin.entity;

import ca.corruptdata.moodyghasts.api.HappyGhastAccessor;
import ca.corruptdata.moodyghasts.api.HappyGhastProjectileShootable;
import ca.corruptdata.moodyghasts.entity.projectile.GhastIceChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
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

@Mixin(HappyGhast.class)
public class HappyGhastMixin implements HappyGhastAccessor {
    
    @Unique
    private static final float INITIAL_MOOD = 60F;
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



@Unique
@Override
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

    @Override
    public boolean moodyghasts$isShooting() {
        HappyGhast ghast = (HappyGhast)(Object)this;
        return ghast.getEntityData().get(IS_SHOOTING);
    }

    @Override
    public float moodyghasts$getMood() {
        HappyGhast ghast = (HappyGhast)(Object)this;
        return ghast.getEntityData().get(MOOD);
    }

    @Unique
    private void moodyghasts$setMood(float mood) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(MOOD, Mth.clamp(mood, 0.0f, 100.0f));
    }

    @Unique
    public void moodyghasts$adjustMood(float delta) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(MOOD, Mth.clamp(moodyghasts$getMood() + delta, 0.0f, 100.0f));
    }

    @Unique
    private void moodyghasts$setShooting(boolean shooting) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(IS_SHOOTING, shooting);
    }
}