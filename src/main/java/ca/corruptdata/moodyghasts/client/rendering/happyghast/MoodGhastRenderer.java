package ca.corruptdata.moodyghasts.client.rendering.happyghast;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.datamap.GhastMoodMap;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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

        if (state.getRenderDataOrThrow(RenderStateKeys.IS_CHARGING)
                || state.getRenderDataOrThrow(RenderStateKeys.IS_SNOWBALL_BARRAGE)) {
            return GHAST_SHOOTING_TEXTURE;
        }

        Map<String, ResourceLocation> textures = GhastMoodMap.getGhastTextures();
        GhastMoodMap map = GhastMoodMap.get();
        if (map == null) return super.getTextureLocation(state);

        String mood = map.getMoodFromValue(state.getRenderDataOrThrow(RenderStateKeys.MOOD));

        // Only fallback if JSON has ghastTexture as null
        if (!textures.containsKey(mood)) {
            return super.getTextureLocation(state);
        }

        // Let Minecraft log a missing texture if the file path is invalid
        return textures.get(mood);
    }
}
