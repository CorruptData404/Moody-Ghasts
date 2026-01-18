package ca.corruptdata.moodyghasts.client.rendering.happyghast;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.client.rendering.RenderStateKeys;
import ca.corruptdata.moodyghasts.moodutil.Mood;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholds;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HappyGhastRenderer;
import net.minecraft.client.renderer.entity.state.HappyGhastRenderState;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class MoodGhastRenderer extends HappyGhastRenderer {

    private static final ResourceLocation GHAST_SHOOTING_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID,
                    "textures/entity/happyghast/ghast_shooting.png");

    private static final Map<Mood, ResourceLocation> GHAST_MOOD_TEXTURES =
            Arrays.stream(Mood.values())
                    .collect(Collectors.toMap(
                            mood -> mood,
                            mood -> ResourceLocation.fromNamespaceAndPath(
                                    MoodyGhasts.MOD_ID,
                                    "textures/entity/happyghast/ghast_" + mood.id() + ".png"
                            )
                    ));

    public MoodGhastRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(HappyGhastRenderState renderState) {
        if (renderState.isBaby)
            return super.getTextureLocation(renderState);

        if (renderState.getRenderDataOrThrow(RenderStateKeys.IS_CHARGING) ||
                renderState.getRenderDataOrThrow(RenderStateKeys.IS_SNOWBALL_BARRAGE))
            return GHAST_SHOOTING_TEXTURE;

        float moodValue = renderState.getRenderDataOrThrow(RenderStateKeys.MOOD);
        Holder<EntityType<?>> holder = EntityType.HAPPY_GHAST.builtInRegistryHolder();
        MoodThresholds thresholds = holder.getData(MoodThresholds.MOOD_THRESHOLDS);
        assert thresholds != null;
        
        Mood mood = thresholds.getMoodFromValue(moodValue);
        
        // "happy" intentionally falls back to default vanilla texture
        if (mood == Mood.HAPPY) return super.getTextureLocation(renderState);
        
        return GHAST_MOOD_TEXTURES.get(mood);
    }
}