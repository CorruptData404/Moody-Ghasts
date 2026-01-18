package ca.corruptdata.moodyghasts.client.rendering.gui;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.HappyGhastHandler;
import ca.corruptdata.moodyghasts.moodutil.Mood;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholds;
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

import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@OnlyIn(Dist.CLIENT)
public class GhastMoodBarRenderer implements ContextualBarRenderer {

    private static final Map<Mood, ResourceLocation> MOOD_BACKGROUND_TEXTURES =
            Arrays.stream(Mood.values())
                    .collect(Collectors.toMap(
                            mood -> mood,
                            mood -> ResourceLocation.fromNamespaceAndPath(
                                    MoodyGhasts.MOD_ID,
                                    "textures/gui/moodbackgroundbar/" + mood.id() + "_background.png"
                            )
                    ));

    private static final Map<Mood, ResourceLocation> MOOD_PROGRESS_TEXTURES =
            Arrays.stream(Mood.values())
                    .collect(Collectors.toMap(
                            mood -> mood,
                            mood -> ResourceLocation.fromNamespaceAndPath(
                                    MoodyGhasts.MOD_ID,
                                    "textures/gui/moodprogressbar/" + mood.id() + "_progress.png"
                            )
                    ));

    // Total size of the bar in pixels
    private static final int BAR_WIDTH = 182;
    private static final int BAR_HEIGHT = 5;

    private final Minecraft minecraft;
    private final HappyGhast happyGhast;
    private final MoodThresholds thresholds;
    private final RandomSource random;

    public GhastMoodBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.happyGhast = (HappyGhast) Objects.requireNonNull(minecraft.player).getVehicle();
        this.thresholds = EntityType.HAPPY_GHAST.builtInRegistryHolder().getData(MoodThresholds.MOOD_THRESHOLDS);
        this.random = minecraft.player.getRandom();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, DeltaTracker deltaTracker) {
        int left = this.left(this.minecraft.getWindow());
        int top = this.top(this.minecraft.getWindow());

        // --- Apply shake and red glow if ghast is enraged ---
        int enragedTicks = happyGhast.getData(ModAttachments.ENRAGED_TICKS);
        if (enragedTicks > 0) {
            float enragedProgress = Mth.clamp((float) enragedTicks / HappyGhastHandler.CRASH_OUT_TICK, 0f, 1f);
            float maxShake = 3.0f;
            float intensity = enragedProgress * maxShake;

            float jitterX = (random.nextFloat() - 0.5f) * 2f * intensity;
            float jitterY = (random.nextFloat() - 0.5f) * 2f * intensity;

            left += (int) jitterX;
            top  += (int) jitterY;

            int glowAlpha = (int) (255 * enragedProgress);
            graphics.fill(left - 1, top - 1, left + BAR_WIDTH + 1, top + BAR_HEIGHT + 1, 0xFF0000 | (glowAlpha << 24));
        }

        float moodValue = this.happyGhast.getData(ModAttachments.MOOD);

        // Draw background sections
        drawMoodSection(graphics, left, top, 0, thresholds.excited(), Mood.EXCITED, moodValue);
        drawMoodSection(graphics, left, top, thresholds.excited(), thresholds.happy(), Mood.HAPPY, moodValue);
        drawMoodSection(graphics, left, top, thresholds.happy(), thresholds.neutral(), Mood.NEUTRAL, moodValue);
        drawMoodSection(graphics, left, top, thresholds.neutral(), thresholds.sad(), Mood.SAD, moodValue);
        drawMoodSection(graphics, left, top, thresholds.sad(), thresholds.angry(), Mood.ANGRY, moodValue);
        drawMoodSection(graphics, left, top, thresholds.angry(), MoodThresholds.MAX, Mood.ENRAGED, moodValue);
    }

    private void drawMoodSection(GuiGraphics graphics, int left, int top, float startThreshold, float endThreshold,
                                 Mood mood, float moodValue) {
        int startPixel = (int) Math.floor((startThreshold / MoodThresholds.MAX) * BAR_WIDTH);
        int endPixel = (int) Math.ceil((endThreshold / MoodThresholds.MAX) * BAR_WIDTH);
        int sectionWidth = endPixel - startPixel;

        if (sectionWidth <= 0) return;

        // Draw background
        ResourceLocation bgTexture = MOOD_BACKGROUND_TEXTURES.get(mood);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED,
                bgTexture,
                left + startPixel,
                top,
                (float) startPixel, 0.0f,
                sectionWidth, BAR_HEIGHT,
                BAR_WIDTH, BAR_HEIGHT
        );

        // Draw progress overlay if needed
        if (moodValue > startThreshold) {
            float fillPercentInSection = (moodValue >= endThreshold)
                    ? 1.0f
                    : (moodValue - startThreshold) / (endThreshold - startThreshold);

            int filledPixels = (int) (sectionWidth * fillPercentInSection);
            if (filledPixels > 0) {
                ResourceLocation fillTexture = MOOD_PROGRESS_TEXTURES.get(mood);
                graphics.blit(
                        RenderPipelines.GUI_TEXTURED,
                        fillTexture,
                        left + startPixel,
                        top,
                        (float) startPixel, 0.0f,
                        filledPixels, BAR_HEIGHT,
                        BAR_WIDTH, BAR_HEIGHT
                );
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {}
}
