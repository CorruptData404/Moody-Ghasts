package ca.corruptdata.moodyghasts.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.world.entity.animal.HappyGhast;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import ca.corruptdata.moodyghasts.client.rendering.gui.GhastMoodBarRenderer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

@Mixin(Gui.class)
public abstract class GuiMixin {

    @Final
    @Shadow
    private Minecraft minecraft;

    @Mutable
    @Shadow
    @Final
    private Map<Gui.ContextualInfo, Supplier<ContextualBarRenderer>> contextualInfoBarRenderers;

    @Unique
    private static Gui.ContextualInfo moodyghasts$updatedEnum;

    @Unique
    private static Gui.ContextualInfo moodyghasts$getUpdatedEnum() {
        if (moodyghasts$updatedEnum == null) {
            moodyghasts$updatedEnum = Arrays.stream(Gui.ContextualInfo.values())
                    .filter(e -> e.name().equals("GHAST_MOOD_BAR"))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("GHAST_MOOD_BAR not found"));
        }
        return moodyghasts$updatedEnum;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectMoodBarRenderer(Minecraft minecraft, CallbackInfo ci) {
        Gui.ContextualInfo ghast = moodyghasts$getUpdatedEnum();

        if (!contextualInfoBarRenderers.containsKey(ghast)) {
            Map<Gui.ContextualInfo, Supplier<ContextualBarRenderer>> map = new HashMap<>(contextualInfoBarRenderers);
            map.put(ghast, () -> new GhastMoodBarRenderer(minecraft));
            contextualInfoBarRenderers = map;
        }
    }


    @Inject(method = "nextContextualInfoState", at = @At("HEAD"), cancellable = true)
    private void injectNextContextualInfoState(CallbackInfoReturnable<Gui.ContextualInfo> cir) {
        if (minecraft.player != null && minecraft.player.getVehicle() instanceof HappyGhast) {
            cir.setReturnValue(moodyghasts$getUpdatedEnum());
        }
    }
}