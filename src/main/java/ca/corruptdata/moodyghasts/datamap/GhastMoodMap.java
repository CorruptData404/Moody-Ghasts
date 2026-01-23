package ca.corruptdata.moodyghasts.datamap;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RegisterSpriteSourcesEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record GhastMoodMap(Map<String, GhastMoodState> moodStates) {
    // ============================================================
    // Constants
    // ============================================================

    public static final float MAX = 1.0f;
    public static final float MIN = 0.0f;

    private static final Codec<Float> PERCENT = Codec.floatRange(MIN, MAX);

    // ============================================================
    // Records
    // ============================================================

    public record GhastMoodState(
            float threshold,
            float proj_multiplier,
            int tantrumTick,
            float speedModifier,
            @Nullable MoodRegression regression,
            String backgroundBarTexture,
            String progressBarTexture,
            @Nullable String ghastTexture
    ) {
        public record MoodRegression(float chance_per_tick, float delta) {
            public static final Codec<MoodRegression> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    PERCENT.fieldOf("chance_per_tick").forGetter(MoodRegression::chance_per_tick),
                    Codec.FLOAT.fieldOf("delta").forGetter(MoodRegression::delta)
            ).apply(inst, MoodRegression::new));
        }

        public static final Codec<GhastMoodState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PERCENT.fieldOf("threshold").forGetter(GhastMoodState::threshold),
                Codec.FLOAT.fieldOf("proj_multiplier").forGetter(GhastMoodState::proj_multiplier),
                Codec.INT.optionalFieldOf("tantrum_tick").forGetter(s -> Optional.of(s.tantrumTick())),
                Codec.FLOAT.optionalFieldOf("speed_modifier").forGetter(s -> Optional.of(s.speedModifier())),
                MoodRegression.CODEC.optionalFieldOf("regression").forGetter(s -> Optional.ofNullable(s.regression())),
                Codec.STRING.fieldOf("background_bar_texture").forGetter(GhastMoodState::backgroundBarTexture),
                Codec.STRING.fieldOf("progress_bar_texture").forGetter(GhastMoodState::progressBarTexture),
                Codec.STRING.optionalFieldOf("ghast_texture").forGetter(s -> Optional.ofNullable(s.ghastTexture()))
        ).apply(inst, (threshold, multiplier, tantrumTick, speedModifier, regression, bgTex, progTex, ghastTex) ->
                new GhastMoodState(threshold, multiplier, tantrumTick.orElse(0),
                        speedModifier.orElse(0.0f), regression.orElse(null),
                        bgTex, progTex, ghastTex.orElse(null))));
    }

    // ============================================================
    // Codec Definition
    // ============================================================

    public static final Codec<GhastMoodMap> CODEC = Codec.unboundedMap(Codec.STRING, GhastMoodState.CODEC)
            .xmap(GhastMoodMap::new, GhastMoodMap::moodStates)
            .flatXmap(GhastMoodMap::validate, DataResult::success);

    public static final DataMapType<EntityType<?>, GhastMoodMap> DATA_MAP = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ghast_mood_map"),
            Registries.ENTITY_TYPE,
            CODEC
    ).synced(CODEC, false).build();

    // ============================================================
    // Public Methods
    // ============================================================

    public static GhastMoodMap get() {
        return EntityType.HAPPY_GHAST.builtInRegistryHolder().getData(DATA_MAP);
    }

    public String getMoodFromValue(float value) {
        if (moodStates.isEmpty()) return null;

        return moodStates.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(
                        Comparator.comparing(GhastMoodState::threshold)))
                .filter(e -> value <= e.getValue().threshold())
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No mood found for value: %.1f (thresholds: %s)",
                                value,
                                moodStates.entrySet().stream()
                                        .map(e -> String.format("%s=%.1f",
                                                e.getKey(), e.getValue().threshold()))
                                        .collect(Collectors.joining(", "))
                        )));
    }

    public int getMoodsTantrumTick(float moodValue) {
        String mood = getMoodFromValue(moodValue);
        return mood != null ? moodStates.get(mood).tantrumTick() : 0;
    }

    public float getMoodsProjMultiplier(float value) {
        String mood = getMoodFromValue(value);
        return mood != null ? moodStates.get(mood).proj_multiplier() : 0.0f;
    }

    public float getSpeedModifier(float value) {
        String mood = getMoodFromValue(value);
        return mood != null ? moodStates.get(mood).speedModifier() : 0.0f;
    }

    public Optional<GhastMoodState.MoodRegression> getMoodRegression(float value) {
        String mood = getMoodFromValue(value);
        if (mood == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(moodStates.get(mood).regression());
    }

    // ============================================================
    // Lazy-loaded textures for all renderers
    // ============================================================

    private static Map<String, ResourceLocation> ghastTextures;
    private static Map<String, ResourceLocation> backgroundTextures;
    private static Map<String, ResourceLocation> progressTextures;

    public static Map<String, ResourceLocation> getGhastTextures() {
        if (ghastTextures == null || ghastTextures.isEmpty()) {
            ghastTextures = buildTextureMap(GhastMoodState::ghastTexture);
        }
        return ghastTextures;
    }

    public static Map<String, ResourceLocation> getBackgroundTextures() {
        if (backgroundTextures == null || backgroundTextures.isEmpty()) {
            backgroundTextures = buildTextureMap(GhastMoodState::backgroundBarTexture);
        }
        return backgroundTextures;
    }

    public static Map<String, ResourceLocation> getProgressTextures() {
        if (progressTextures == null || progressTextures.isEmpty()) {
            progressTextures = buildTextureMap(GhastMoodState::progressBarTexture);
        }
        return progressTextures;
    }

    private static Map<String, ResourceLocation> buildTextureMap(java.util.function.Function<GhastMoodState, String> extractor) {
        GhastMoodMap map = get();
        if (map == null) return Map.of();
        return map.moodStates().entrySet().stream()
                .filter(e -> extractor.apply(e.getValue()) != null)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> ResourceLocation.parse(extractor.apply(e.getValue()))
                ));
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        clearTextures();
    }

    @SubscribeEvent
    public static void onResourceManagerReload(RegisterSpriteSourcesEvent event) {
        clearTextures();
    }

    private static void clearTextures() {
        if (ghastTextures != null) ghastTextures.clear();
        if (backgroundTextures != null) backgroundTextures.clear();
        if (progressTextures != null) progressTextures.clear();
    }

    // ============================================================
    // Validation
    // ============================================================
    private static DataResult<GhastMoodMap> validate(GhastMoodMap ghastMoodMap) {
        if (ghastMoodMap.moodStates().isEmpty()) {
            return DataResult.error(() -> "Mood states cannot be empty");
        }

        // Validate threshold ranges and check for duplicates
        Set<Float> thresholds = new HashSet<>();
        for (var entry : ghastMoodMap.moodStates().entrySet()) {
            float threshold = entry.getValue().threshold();
            
            if (threshold < MIN || threshold > MAX) {
                return DataResult.error(() ->
                        String.format("Threshold for %s (%.1f) must be between %.1f and %.1f",
                                entry.getKey(), threshold, MIN, MAX));
            }
            
            if (!thresholds.add(threshold)) {
                return DataResult.error(() ->
                        String.format("Duplicate threshold value %.1f for mood %s",
                                threshold, entry.getKey()));
            }
        }

        // Verify exactly one mood has MAX threshold
        long maxThresholdCount = thresholds.stream()
                .filter(t -> t == MAX)
                .count();
            
        if (maxThresholdCount != 1) {
            return DataResult.error(() ->
                    String.format("Exactly one mood must have threshold of %.1f, found %d",
                            MAX, maxThresholdCount));
        }

        // Check ascending order
        float previousThreshold = MIN - 1;
        List<Map.Entry<String, GhastMoodState>> sortedStates = ghastMoodMap.moodStates().entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(GhastMoodState::threshold)))
                .toList();

        for (var entry : sortedStates) {
            float currentThreshold = entry.getValue().threshold();
            if (currentThreshold <= previousThreshold) {
                float finalPreviousThreshold = previousThreshold;
                return DataResult.error(() ->
                        String.format("Mood thresholds must be ascending. %s (%.1f) <= previous (%.1f)",
                                entry.getKey(), currentThreshold, finalPreviousThreshold));
            }
            previousThreshold = currentThreshold;
        }

        return DataResult.success(ghastMoodMap);
    }
}