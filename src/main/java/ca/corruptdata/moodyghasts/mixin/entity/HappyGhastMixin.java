package ca.corruptdata.moodyghasts.mixin.entity;

import ca.corruptdata.moodyghasts.api.HappyGhastAccessor;
import ca.corruptdata.moodyghasts.entity.projectile.PlayerIceChargeEntity;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.animal.HappyGhast;
import net.minecraft.world.entity.player.Player;
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

    @Inject(method = "defineSynchedData(Lnet/minecraft/network/syncher/SynchedEntityData$Builder;)V", at = @At("TAIL"))
    private void defineCustomData(SynchedEntityData.Builder builder, CallbackInfo ci) {
        builder.define(IS_SHOOTING, false);
        builder.define(MOOD, INITIAL_MOOD);
    }



    @Unique
    @Override
    public boolean moodyghasts$beginShooting(Player player) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        
        if (!ghast.level().isClientSide && ghast.isVehicle() && 
            player == ghast.getFirstPassenger()) {
            
            if (moodyghasts$shootChargeTime == 0) { // Only start if not already charging
                moodyghasts$shootChargeTime = 1; // Start charging
                return true;
            }
        }
        return false;
    }

@Inject(method = "tick", at = @At("HEAD"))
private void onTick(CallbackInfo ci) {
    if (moodyghasts$shootChargeTime > 0) {
        moodyghasts$setShooting(true);
        moodyghasts$shootChargeTime++;

        if (moodyghasts$shootChargeTime == 10) {
            HappyGhast ghast = (HappyGhast)(Object)this;
            ghast.level().levelEvent(null, 1015, ghast.blockPosition(), 0);
        }
        
        if (moodyghasts$shootChargeTime == 20) {
            HappyGhast ghast = (HappyGhast)(Object)this;
            if (ghast.isVehicle() && ghast.getFirstPassenger() instanceof Player player) {
                moodyghasts$shootIceCharge(player, ghast);
            }
            moodyghasts$shootChargeTime = 0;
            moodyghasts$setShooting(false);
        }
    }
}

@Unique
private void moodyghasts$shootIceCharge(Player player, HappyGhast ghast) {
        if (ghast.level() instanceof ServerLevel serverLevel) {
            PlayerIceChargeEntity iceCharge = new PlayerIceChargeEntity(player, serverLevel);

            // Get player's view vector
            float xRot = Math.max(-75, Math.min(75, player.getXRot())); // Clamp vertical rotation
            Vec3 viewVector = Vec3.directionFromRotation(xRot, player.getYRot());

            // Spawn projectile 4 blocks in front of the ghast (matching hostile ghast)
            iceCharge.setPos(
                ghast.getX() + viewVector.x * 4.0,
                ghast.getY() + ghast.getEyeHeight() - 0.5,
                ghast.getZ() + viewVector.z * 4.0
            );

            // Shoot using the player's aim direction
            iceCharge.shoot(
                viewVector.x,
                viewVector.y,
                viewVector.z,
                IceChargeItem.PROJECTILE_SHOOT_POWER,
                0.0F // No inaccuracy
            );

            serverLevel.addFreshEntity(iceCharge);
            
            ghast.level().levelEvent(null, 1016, ghast.blockPosition(), 0);

            moodyghasts$shootChargeTime = -1;
        }
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
        ghast.getEntityData().set(MOOD, mood);
    }

    @Unique
    private void moodyghasts$adjustMood(float delta) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        float newMood = Math.min(100.0f, Math.max(0.0f, moodyghasts$getMood() + delta));
        ghast.getEntityData().set(MOOD, newMood);
    }

    @Unique
    private void moodyghasts$setShooting(boolean shooting) {
        HappyGhast ghast = (HappyGhast)(Object)this;
        ghast.getEntityData().set(IS_SHOOTING, shooting);
    }
}