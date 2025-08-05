package ca.corruptdata.moodyghasts.mixin.rendering;

import ca.corruptdata.moodyghasts.util.MoodThresholds;
import ca.corruptdata.moodyghasts.util.MoodThresholdsManager;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.api.HappyGhastRenderStateAccessor;
import ca.corruptdata.moodyghasts.api.HappyGhastAccessor;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.HappyGhast;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HappyGhastRenderer.class)
public class HappyGhastRendererMixin {
    @Shadow @Final private static ResourceLocation GHAST_BABY_LOCATION;
    @Shadow @Final private static ResourceLocation GHAST_LOCATION;

    @Unique
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_shooting.png");
    @Unique
    private static final ResourceLocation GHAST_EXCITED_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_excited.png");
    @Unique
    private static final ResourceLocation GHAST_NEUTRAL_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_neutral.png");
    @Unique
    private static final ResourceLocation GHAST_SAD_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_sad.png");
    @Unique
    private static final ResourceLocation GHAST_ANGRY_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_angry.png");
    @Unique
    private static final ResourceLocation GHAST_ENRAGED_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_enraged.png");

    /**
     * @author CorruptData
     * @reason Add mood-based textures for ghasts
     */
    @Overwrite
    public ResourceLocation getTextureLocation(HappyGhastRenderState ghastState) {
        if (ghastState.isBaby) {
            return GHAST_BABY_LOCATION;
        }

        HappyGhastRenderStateAccessor accessor = (HappyGhastRenderStateAccessor) ghastState;

        if(accessor.isMoodyghasts$isShooting()) {
            return GHAST_SHOOTING_LOCATION;
        }
        float mood = accessor.getMoodyghasts$mood();
        MoodThresholds thresholds = MoodThresholdsManager.getCurrentInstance();

        if (mood <= thresholds.getMoodValue("excited")) return GHAST_EXCITED_LOCATION;
        if (mood <= thresholds.getMoodValue("happy")) return GHAST_LOCATION;
        if (mood <= thresholds.getMoodValue("neutral")) return GHAST_NEUTRAL_LOCATION;
        if (mood <= thresholds.getMoodValue("sad")) return GHAST_SAD_LOCATION;
        if (mood <= thresholds.getMoodValue("angry")) return GHAST_ANGRY_LOCATION;
        return GHAST_ENRAGED_LOCATION;
    }

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/animal/HappyGhast;Lnet/minecraft/client/renderer/entity/state/HappyGhastRenderState;F)V", at = @At("TAIL"))
    private void extractRenderState(HappyGhast ghast, HappyGhastRenderState renderState, float partialTick, CallbackInfo ci) {
        HappyGhastAccessor ghastShooter = (HappyGhastAccessor) ghast;
        HappyGhastRenderStateAccessor accessor = (HappyGhastRenderStateAccessor) renderState;
        
        accessor.setMoodyghasts$isShooting(ghastShooter.moodyghasts$isShooting());
        accessor.setMoodyghasts$mood(ghastShooter.moodyghasts$getMood());
    }
}