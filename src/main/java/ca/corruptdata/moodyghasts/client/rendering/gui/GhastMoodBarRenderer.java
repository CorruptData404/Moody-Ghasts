package ca.corruptdata.moodyghasts.client.rendering.gui;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.datamap.GhastMoodMap;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.HappyGhast;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.*;

@OnlyIn(Dist.CLIENT)
public class GhastMoodBarRenderer implements ContextualBarRenderer {

    private final Minecraft minecraft;
    private final HappyGhast happyGhast;
    private final GhastMoodMap thresholds;
    private final RandomSource random;
    private final Map<String, ResourceLocation> moodBackgroundTextures;
    private final Map<String, ResourceLocation> moodProgressTextures;

    public GhastMoodBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.happyGhast = (HappyGhast) Objects.requireNonNull(minecraft.player).getVehicle();
        this.thresholds = EntityType.HAPPY_GHAST.builtInRegistryHolder().getData(GhastMoodMap.DATA_MAP);
        this.random = minecraft.player.getRandom();

        // Initialize texture maps based on available moods
        this.moodBackgroundTextures = GhastMoodMap.getBackgroundTextures();
        this.moodProgressTextures = GhastMoodMap.getProgressTextures();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, DeltaTracker deltaTracker) {
        int left = this.left(this.minecraft.getWindow());
        int top = this.top(this.minecraft.getWindow());

        float moodValue = this.happyGhast.getData(ModAttachments.MOOD);

        // Apply shake and glow effects if the current mood has a tantrumTick
        int tantrumTick = thresholds.getMoodsTantrumTick(moodValue);
        int enragedTicks = happyGhast.getData(ModAttachments.TANTRUM_TICKS);
        if (enragedTicks > 0) {
            float progress = Mth.clamp((float) enragedTicks / tantrumTick, 0f, 1f);
            float maxShake = 3.0f;
            float intensity = progress * maxShake;

            float jitterX = (random.nextFloat() - 0.5f) * 2f * intensity;
            float jitterY = (random.nextFloat() - 0.5f) * 2f * intensity;

            left += (int) jitterX;
            top += (int) jitterY;

            int glowAlpha = (int) (255 * progress);
            graphics.fill(left - 1, top - 1, left + WIDTH + 1, top + HEIGHT + 1,
                    0xFF0000 | (glowAlpha << 24));
        }

        // Sort mood states by threshold to ensure correct rendering order
        List<Map.Entry<String, GhastMoodMap.GhastMoodState>> sortedMoods = thresholds.moodStates().entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(GhastMoodMap.GhastMoodState::threshold)))
                .toList();

        float prevThreshold = GhastMoodMap.MIN;
        for (var entry : sortedMoods) {
            String mood = entry.getKey();
            float threshold = entry.getValue().threshold();
            drawMoodSection(graphics, left, top, prevThreshold, threshold, mood, moodValue);
            prevThreshold = threshold;
        }
    }

    private void drawMoodSection(GuiGraphics graphics, int left, int top, float startThreshold,
                               float endThreshold, String mood, float moodValue) {
        int startPixel = (int) Math.floor(startThreshold * WIDTH);
        int endPixel = (int) Math.ceil(endThreshold * WIDTH);
        int sectionWidth = endPixel - startPixel;

        if (sectionWidth <= 0) return;

        // Draw background if texture exists
        ResourceLocation bgTexture = moodBackgroundTextures.get(mood);
        if (bgTexture != null) {
            graphics.blit(
                    RenderPipelines.GUI_TEXTURED,
                    bgTexture,
                    left + startPixel,
                    top,
                    (float) startPixel, 0.0f,
                    sectionWidth, HEIGHT,
                    WIDTH, HEIGHT
            );
        }

        // Draw progress overlay if needed and texture exists
        if (moodValue > startThreshold) {
            float fillPercentInSection = (moodValue >= endThreshold)
                    ? 1.0f
                    : (moodValue - startThreshold) / (endThreshold - startThreshold);

            int filledPixels = (int) (sectionWidth * fillPercentInSection);
            if (filledPixels > 0) {
                ResourceLocation fillTexture = moodProgressTextures.get(mood);
                if (fillTexture != null) {
                    graphics.blit(
                            RenderPipelines.GUI_TEXTURED,
                            fillTexture,
                            left + startPixel,
                            top,
                            (float) startPixel, 0.0f,
                            filledPixels, HEIGHT,
                            WIDTH, HEIGHT
                    );
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {}
}