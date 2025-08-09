package ca.corruptdata.moodyghasts.client.rendering.happyghast;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.util.MoodThresholds;
import ca.corruptdata.moodyghasts.util.MoodThresholdsManager;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.resources.ResourceLocation;

public class MoodGhastRenderer extends HappyGhastRenderer {
    private static final ResourceLocation GHAST_SHOOTING_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_shooting.png");
    private static final ResourceLocation GHAST_EXCITED_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_excited.png");
    private static final ResourceLocation GHAST_NEUTRAL_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_neutral.png");
    private static final ResourceLocation GHAST_SAD_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_sad.png");
    private static final ResourceLocation GHAST_ANGRY_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_angry.png");
    private static final ResourceLocation GHAST_ENRAGED_LOCATION = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,"textures/entity/happyghast/ghast_enraged.png");

    public MoodGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HappyGhastRenderState renderState) {

        if (renderState.isBaby) super.getTextureLocation(renderState);
        if (renderState.getRenderDataOrThrow(RenderStateKeys.IS_CHARGING)) return GHAST_SHOOTING_LOCATION;


        float mood = renderState.getRenderDataOrThrow(RenderStateKeys.MOOD);
        MoodThresholds thresholds = MoodThresholdsManager.getCurrentInstance();

        if (mood <= thresholds.getMoodValue("excited")) return GHAST_EXCITED_LOCATION;
        else if (mood <= thresholds.getMoodValue("happy")) return super.getTextureLocation(renderState);
        else if (mood <= thresholds.getMoodValue("neutral")) return GHAST_NEUTRAL_LOCATION;
        else if (mood <= thresholds.getMoodValue("sad")) return GHAST_SAD_LOCATION;
        else if (mood <= thresholds.getMoodValue("angry")) return GHAST_ANGRY_LOCATION;
        else return GHAST_ENRAGED_LOCATION;
    }
}