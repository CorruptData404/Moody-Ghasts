package ca.corruptdata.moodyghasts.mixin;

import ca.corruptdata.moodyghasts.api.IceChargeConvertible;
import net.minecraft.world.entity.monster.Skeleton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Skeleton.class)
public abstract class SkeletonFreezeConversionMixin implements IceChargeConvertible {
    @Unique
    private boolean moody_Ghasts$isIceChargeConverting = false;

    @Shadow private int inPowderSnowTime;
    @Shadow private int conversionTime;

    @Shadow public abstract boolean isFreezeConverting();
    @Shadow public abstract void setFreezeConverting(boolean converting);

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (this.moody_Ghasts$isIceChargeConverting && !this.isFreezeConverting()) {
            this.inPowderSnowTime = 300;
            this.conversionTime = ICE_CHARGE_CONVERSION_TIME;
            this.setFreezeConverting(true);
        }
    }

    @Redirect(
            method = "tick",
            at = @At(value = "FIELD", target = "net.minecraft.world.entity.monster.Skeleton.isInPowderSnow:Z", ordinal = 0)
    )
    private boolean redirectIsInPowderSnow(Skeleton skeleton) {
        return skeleton.isInPowderSnow || this.moody_Ghasts$isIceChargeConverting;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void suppressVanillaConversionReset(CallbackInfo ci) {
        if (this.moody_Ghasts$isIceChargeConverting && !this.isFreezeConverting()) {
            this.setFreezeConverting(true);
        }
    }

    @Inject(method = "doFreezeConversion", at = @At("TAIL"))
    private void onFinishConversion(CallbackInfo ci) {
        this.moody_Ghasts$isIceChargeConverting = false;
    }

    @Override
    public void moody_Ghasts$startIceChargeConversion() {
        this.moody_Ghasts$isIceChargeConverting = true;
    }
}