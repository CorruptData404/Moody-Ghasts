package ca.corruptdata.moodyghasts.entity.happy_ghast.data;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record GhastMoodMap(GhastMoodSettings settings, Map<ResourceLocation, GhastMoodState> moodStates) {
    // ============================================================
    // Constants
    // ============================================================

    public static final float MAX = 1.0f;
    public static final float MIN = 0.0f;

    private static final Codec<Float> PERCENT = Codec.floatRange(MIN, MAX);

    // ============================================================
    // Records
    // ============================================================

    public record GhastMoodSettings(
            float baseMood,
            float damageMoodRate,
            float healMoodRate
    ) {
        public static final Codec<GhastMoodSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PERCENT.fieldOf("base_mood").forGetter(GhastMoodSettings::baseMood),
                Codec.FLOAT.fieldOf("damage_mood_mult").forGetter(GhastMoodSettings::damageMoodRate),
                Codec.FLOAT.fieldOf("heal_mood_mult").forGetter(GhastMoodSettings::healMoodRate)
        ).apply(inst, GhastMoodSettings::new));
    }

    public record GhastMoodState(
            float threshold,
            int tantrumTick,
            float speedModifier,
            @Nullable MoodRegression regression,
            ResourceLocation backgroundBarTexture,
            ResourceLocation progressBarTexture,
            @Nullable ResourceLocation ghastTexture
    ) {
        public record MoodRegression(float chance_per_tick, float delta) {
            public static final Codec<MoodRegression> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    PERCENT.fieldOf("chance_per_tick").forGetter(MoodRegression::chance_per_tick),
                    Codec.FLOAT.fieldOf("delta").forGetter(MoodRegression::delta)
            ).apply(inst, MoodRegression::new));
        }

        public static final Codec<GhastMoodState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PERCENT.fieldOf("threshold").forGetter(GhastMoodState::threshold),
                Codec.INT.optionalFieldOf("tantrum_tick").forGetter(s -> Optional.of(s.tantrumTick())),
                Codec.FLOAT.optionalFieldOf("speed_modifier").forGetter(s -> Optional.of(s.speedModifier())),
                MoodRegression.CODEC.optionalFieldOf("regression").forGetter(s -> Optional.ofNullable(s.regression())),
                ResourceLocation.CODEC.fieldOf("background_bar_texture").forGetter(GhastMoodState::backgroundBarTexture),
                ResourceLocation.CODEC.fieldOf("progress_bar_texture").forGetter(GhastMoodState::progressBarTexture),
                ResourceLocation.CODEC.optionalFieldOf("ghast_texture").forGetter(s -> Optional.ofNullable(s.ghastTexture()))
        ).apply(inst, (threshold, tantrumTick,
                       speedModifier, regression,
                       bgTex, progTex,
                       ghastTex) ->
                new GhastMoodState(threshold, tantrumTick.orElse(0),
                        speedModifier.orElse(0.0f), regression.orElse(null),
                        bgTex, progTex, ghastTex.orElse(null))));
    }

    // ============================================================
    // Codec Definition
    // ============================================================

    public static final Codec<GhastMoodMap> CODEC = RecordCodecBuilder.<GhastMoodMap>create(inst -> inst.group(
                    GhastMoodSettings.CODEC.fieldOf("settings").forGetter(GhastMoodMap::settings),
                    Codec.unboundedMap(ResourceLocation.CODEC, GhastMoodState.CODEC)
                            .fieldOf("mood_states").forGetter(GhastMoodMap::moodStates)
            ).apply(inst, GhastMoodMap::new))
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

    private static float cachedBaseMood = 0.25f;

    public static float getBaseMood() {
        return cachedBaseMood;
    }

    @SubscribeEvent
    public static void onDatapackSync(OnDatapackSyncEvent event) {
        GhastMoodMap map = get();
        if (map != null) {
            cachedBaseMood = map.settings().baseMood();
        }
    }

    public ResourceLocation getMoodOfValue(float moodValue) {
        if (moodStates.isEmpty()) return null;

        return moodStates.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.comparing(GhastMoodState::threshold)))
                .filter(e -> moodValue <= e.getValue().threshold())
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No mood found for value: %.1f", moodValue)));
    }

    public float getThresholdValueOfMood(float moodValue) {
        ResourceLocation mood = getMoodOfValue(moodValue);
        return moodStates.get(mood).threshold();
    }

    public int getTantrumTick(float moodValue) {
        ResourceLocation mood = getMoodOfValue(moodValue);
        return mood != null ? moodStates.get(mood).tantrumTick() : 0;
    }

    public float getSpeedModifier(float moodValue) {
        ResourceLocation mood = getMoodOfValue(moodValue);
        return mood != null ? moodStates.get(mood).speedModifier() : 0.0f;
    }

    public Optional<GhastMoodState.MoodRegression> getMoodRegression(float moodValue) {
        ResourceLocation mood = getMoodOfValue(moodValue);
        if (mood == null) return Optional.empty();
        return Optional.ofNullable(moodStates.get(mood).regression());
    }

    public Map<ResourceLocation, ResourceLocation> getGhastTextures() {
        return moodStates.entrySet().stream()
                .filter(e -> e.getValue().ghastTexture() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().ghastTexture()));
    }

    public Map<ResourceLocation, ResourceLocation> getBackgroundTextures() {
        return moodStates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().backgroundBarTexture()));
    }

    public Map<ResourceLocation, ResourceLocation> getProgressTextures() {
        return moodStates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().progressBarTexture()));
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
        List<Map.Entry<ResourceLocation, GhastMoodState>> sortedStates = ghastMoodMap.moodStates().entrySet().stream()
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