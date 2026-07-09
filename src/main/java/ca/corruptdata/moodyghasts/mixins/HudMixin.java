package ca.corruptdata.moodyghasts.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.contextualbar.ContextualBar;
import net.minecraft.world.entity.animal.happyghast.HappyGhast;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ca.corruptdata.moodyghasts.client.rendering.gui.GhastMoodBar;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(Hud.class)
public abstract class HudMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Mutable
    @Shadow
    @Final
    private Map<Hud.ContextualInfo, Supplier<ContextualBar>> contextualInfoBars;

    @Unique
    private static Hud.ContextualInfo moodyghasts$updatedEnum;

    @Unique
    private static Hud.ContextualInfo moodyghasts$getUpdatedEnum() {
        if (moodyghasts$updatedEnum == null) {
            moodyghasts$updatedEnum = Arrays.stream(Hud.ContextualInfo.values())
                    .filter(e -> e.name().equals("MOODY_VEHICLE"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("MOODY_VEHICLE not found"));
        }
        return moodyghasts$updatedEnum;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectMoodBarRenderer(Minecraft minecraft, CallbackInfo ci) {
        Hud.ContextualInfo ghast = moodyghasts$getUpdatedEnum();

        if (!contextualInfoBars.containsKey(ghast)) {
            Map<Hud.ContextualInfo, Supplier<ContextualBar>> map = new HashMap<>(contextualInfoBars);
            map.put(ghast, () -> new GhastMoodBar(minecraft));
            contextualInfoBars = map;
        }
    }

    @Inject(method = "nextContextualInfoState", at = @At("HEAD"), cancellable = true)
    private void injectNextContextualInfoState(CallbackInfoReturnable<Hud.ContextualInfo> cir) {
        if (minecraft.player != null && minecraft.player.getVehicle() instanceof HappyGhast) {
            cir.setReturnValue(moodyghasts$getUpdatedEnum());
        }
    }
}