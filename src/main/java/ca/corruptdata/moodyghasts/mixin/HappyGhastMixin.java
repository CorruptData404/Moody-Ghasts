package ca.corruptdata.moodyghasts.mixin;

import ca.corruptdata.moodyghasts.api.HappyGhastShooter;
import ca.corruptdata.moodyghasts.entity.projectile.PlayerIceCharge;
import ca.corruptdata.moodyghasts.item.custom.IceChargeItem;
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
public class HappyGhastMixin implements HappyGhastShooter {
    @Unique
    private int moodyghasts$shootChargeTime = 0; // Changed to 0 to match vanilla behavior
    
    @Unique
    @Override
    public boolean moodyghasts$tryShootIceCharge(Player player) {
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
            moodyghasts$shootChargeTime++;
            
            // Add warning sound at tick 10 to match hostile ghast
            if (moodyghasts$shootChargeTime == 10) {
                HappyGhast ghast = (HappyGhast)(Object)this;
                ghast.level().levelEvent(null, 1015, ghast.blockPosition(), 0);
            }
            
            if (moodyghasts$shootChargeTime == 20) {
                HappyGhast ghast = (HappyGhast)(Object)this;
                if (ghast.isVehicle() && ghast.getFirstPassenger() instanceof Player player) {
                    moodyghasts$shootIceCharge(player, ghast);
                }
                moodyghasts$shootChargeTime = 0; // Reset to allow new charges
            }
        }
    }

@Unique
private void moodyghasts$shootIceCharge(Player player, HappyGhast ghast) {
    if (ghast.level() instanceof ServerLevel serverLevel) {
        PlayerIceCharge iceCharge = new PlayerIceCharge(player, serverLevel);
        
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
        
        // Play shoot sound
        ghast.level().levelEvent(null, 1016, ghast.blockPosition(), 0);
        
        moodyghasts$shootChargeTime = -1;
    }
}
}