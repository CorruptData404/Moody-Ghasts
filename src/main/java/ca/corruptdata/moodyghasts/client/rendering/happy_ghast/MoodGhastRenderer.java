package ca.corruptdata.moodyghasts.client.rendering.happy_ghast;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public class MoodGhastRenderer extends HappyGhastRenderer {

    // Default shooting texture
    private static final ResourceLocation GHAST_SHOOTING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,
                    "textures/entity/happyghast/ghast_shooting.png");

    public MoodGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(HappyGhastRenderState state) {
        if (state.isBaby) return super.getTextureLocation(state);

        if (state.getRenderDataOrThrow(RenderStateKeys.IS_CHARGING) || state.getRenderDataOrThrow(RenderStateKeys.IS_BARRAGING))
            return GHAST_SHOOTING_TEXTURE;

        GhastMoodMap map = GhastMoodMap.get();
        if (map == null) return super.getTextureLocation(state);
        ResourceLocation mood = map.getMoodOfValue(state.getRenderDataOrThrow(RenderStateKeys.MOOD));
        ResourceLocation texture = map.moodStates().get(mood).ghastTexture();
        return texture != null ? texture : super.getTextureLocation(state);
    }
}
