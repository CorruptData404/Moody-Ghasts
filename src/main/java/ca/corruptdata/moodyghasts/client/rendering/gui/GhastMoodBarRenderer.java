package ca.corruptdata.moodyghasts.client.rendering.gui;

import ca.corruptdata.moodyghasts.ModAttachments;
import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.moodutil.Mood;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholds;
import ca.corruptdata.moodyghasts.moodutil.MoodThresholdsManager;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.contextualbar.ContextualBarRenderer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.ResourceLocation;
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

    public GhastMoodBarRenderer(Minecraft minecraft) {
        this.minecraft = minecraft;
        this.happyGhast = (HappyGhast) Objects.requireNonNull(minecraft.player).getVehicle();
        this.thresholds = MoodThresholdsManager.getCurrentInstance();
    }

    @Override
    public void renderBackground(GuiGraphics graphics, DeltaTracker deltaTracker) {
        if (this.happyGhast == null || this.minecraft.player == null) return;

        int left = this.left(this.minecraft.getWindow());
        int top = this.top(this.minecraft.getWindow());

        // --- Get the ghast’s current mood (client-side synced attachment) ---
        float moodValue = this.happyGhast.getData(ModAttachments.MOOD); // 0–100 range

        float prevThreshold = 0f;
        Mood[] moods = Mood.values();

        for (int i = 0; i < moods.length; i++) {
            Mood mood = moods[i];
            float currentThreshold = this.thresholds.get(mood); // 0–100 range

            // Convert thresholds to pixel coordinates
            int startPixel = (int) Math.floor((prevThreshold / 100f) * BAR_WIDTH);
            int endPixel   = (int) Math.ceil((currentThreshold / 100f) * BAR_WIDTH);
            if (i == moods.length - 1) endPixel = BAR_WIDTH;
            int sectionWidth = endPixel - startPixel;
            if (sectionWidth <= 0) {
                prevThreshold = currentThreshold;
                continue;
            }

            // --- Draw background texture ---
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

            // --- Draw progress overlay ---
            if (moodValue > prevThreshold) {
                float fillPercentInSection = (moodValue >= currentThreshold)
                        ? 1.0f
                        : (moodValue - prevThreshold) / (currentThreshold - prevThreshold);

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

            prevThreshold = currentThreshold;
        }
    }

    @Override
    public void render(GuiGraphics graphics, DeltaTracker deltaTracker) {}
}
