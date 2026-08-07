package ca.corruptdata.moodyghasts.entity.happy_ghast.data;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.stream.Collectors;

public record GhastMoodMap(GhastMoodSettings settings, Map<Identifier, GhastMoodState> moodStates) {
    // ============================================================
    // Constants
    // ============================================================

    public static final float MAX = 1.0f;
    public static final float MIN = 0.0f;

    private static final Codec<Float> PERCENT = Codec.floatRange(MIN, MAX);
    private static final Codec<Float> NON_NEGATIVE_FLOAT = Codec.floatRange(0f, Float.MAX_VALUE);
    private static final Codec<Integer> NON_NEGATIVE_INT = Codec.intRange(0, Integer.MAX_VALUE);

    // ============================================================
    // Records
    // ============================================================

    public record GhastMoodSettings(
            float baseMood,
            float damageMoodMult,
            float healMoodMult,
            float moodEventRadius,
            float seeBabyDeathDelta,
            float killBabyDelta,
            float seeAdultDeathDelta,
            float killAdultDelta
    ) {
        public static final Codec<GhastMoodSettings> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PERCENT.fieldOf("base_mood").forGetter(GhastMoodSettings::baseMood),
                Codec.FLOAT.fieldOf("damage_mood_mult").forGetter(GhastMoodSettings::damageMoodMult),
                Codec.FLOAT.fieldOf("heal_mood_mult").forGetter(GhastMoodSettings::healMoodMult),
                NON_NEGATIVE_FLOAT.fieldOf("mood_event_radius").forGetter(GhastMoodSettings::moodEventRadius),
                Codec.FLOAT.fieldOf("see_baby_death_delta").forGetter(GhastMoodSettings::seeBabyDeathDelta),
                Codec.FLOAT.fieldOf("kill_baby_delta").forGetter(GhastMoodSettings::killBabyDelta),
                Codec.FLOAT.fieldOf("see_adult_death_delta").forGetter(GhastMoodSettings::seeAdultDeathDelta),
                Codec.FLOAT.fieldOf("kill_adult_delta").forGetter(GhastMoodSettings::killAdultDelta)
        ).apply(inst, GhastMoodSettings::new));
    }

    public record GhastMoodState(
            float threshold,
            @Nullable Integer tantrumTick,
            @Nullable Float speedModifier,
            @Nullable MoodRegression regression,
            Identifier backgroundBarTexture,
            Identifier progressBarTexture,
            @Nullable Identifier ghastTexture
    ) {
        public record MoodRegression(float chance_per_tick, float delta) {
            public static final Codec<MoodRegression> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                    PERCENT.fieldOf("chance_per_tick").forGetter(MoodRegression::chance_per_tick),
                    NON_NEGATIVE_FLOAT.fieldOf("delta").forGetter(MoodRegression::delta)
            ).apply(inst, MoodRegression::new));
        }

        public static final Codec<GhastMoodState> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                PERCENT.fieldOf("threshold").forGetter(GhastMoodState::threshold),
                NON_NEGATIVE_INT.optionalFieldOf("tantrum_tick").forGetter(s -> Optional.ofNullable(s.tantrumTick())),
                Codec.FLOAT.optionalFieldOf("speed_modifier").forGetter(s -> Optional.ofNullable(s.speedModifier())),
                MoodRegression.CODEC.optionalFieldOf("regression").forGetter(s -> Optional.ofNullable(s.regression())),
                Identifier.CODEC.fieldOf("background_bar_texture").forGetter(GhastMoodState::backgroundBarTexture),
                Identifier.CODEC.fieldOf("progress_bar_texture").forGetter(GhastMoodState::progressBarTexture),
                Identifier.CODEC.optionalFieldOf("ghast_texture").forGetter(s -> Optional.ofNullable(s.ghastTexture()))
        ).apply(inst, (threshold, tantrumTick,
                       speedModifier, regression,
                       bgTex, progTex,
                       ghastTex) ->
                new GhastMoodState(threshold, tantrumTick.orElse(null),
                        speedModifier.orElse(null), regression.orElse(null),
                        bgTex, progTex, ghastTex.orElse(null))));
    }

    // ============================================================
    // Codec Definition
    // ============================================================

    public static final Codec<GhastMoodMap> CODEC = RecordCodecBuilder.<GhastMoodMap>create(inst -> inst.group(
                    GhastMoodSettings.CODEC.fieldOf("settings").forGetter(GhastMoodMap::settings),
                    Codec.unboundedMap(Identifier.CODEC, GhastMoodState.CODEC)
                            .fieldOf("mood_states").forGetter(GhastMoodMap::moodStates)
            ).apply(inst, GhastMoodMap::new))
            .flatXmap(GhastMoodMap::validate, DataResult::success);

    public static final DataMapType<EntityType<?>, GhastMoodMap> DATA_MAP = DataMapType.builder(
            Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "ghast_mood_map"),
            Registries.ENTITY_TYPE,
            CODEC
    ).synced(CODEC, false).build();

    // ============================================================
    // Sorted-state cache
    // ============================================================
    private static GhastMoodMap cachedFor;
    private static List<Map.Entry<Identifier, GhastMoodState>> cachedSortedStates;

    private List<Map.Entry<Identifier, GhastMoodState>> sortedStates() {
        if (cachedFor != this) {
            cachedSortedStates = moodStates.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.comparing(GhastMoodState::threshold)))
                    .toList();
            cachedFor = this;
        }
        return cachedSortedStates;
    }

    // ============================================================
    // Public Methods
    // ============================================================

    public static GhastMoodMap get() {
        return EntityType.HAPPY_GHAST.builtInRegistryHolder().getData(DATA_MAP);
    }

    public Identifier getMoodOfValue(float moodValue) {
        if (moodStates.isEmpty()) return null;

        return sortedStates().stream()
                .filter(e -> moodValue <= e.getValue().threshold())
                .findFirst()
                .map(Map.Entry::getKey)
                .orElseThrow(() -> new IllegalStateException(
                        String.format("No mood found for value: %.1f", moodValue)));
    }

    public float getThresholdValueOfMood(float moodValue) {
        Identifier mood = getMoodOfValue(moodValue);
        return moodStates.get(mood).threshold();
    }


    public int getTantrumTick(float moodValue) {
        Identifier mood = getMoodOfValue(moodValue);
        if (mood == null) return 0;
        Integer tantrumTick = moodStates.get(mood).tantrumTick();
        if (tantrumTick == null) return 0;
        return tantrumTick;
    }

    public float getSpeedModifier(float moodValue) {
        Identifier mood = getMoodOfValue(moodValue);
        if (mood == null) return 0.0f;
        Float speedModifier = moodStates.get(mood).speedModifier();
        if (speedModifier == null) return 0.0f;
        return speedModifier;
    }


    public Optional<GhastMoodState.MoodRegression> getMoodRegression(float moodValue) {
        Identifier mood = getMoodOfValue(moodValue);
        if (mood == null) return Optional.empty();
        return Optional.ofNullable(moodStates.get(mood).regression());
    }

    public Map<Identifier, Identifier> getGhastTextures() {
        return moodStates.entrySet().stream()
                .filter(e -> e.getValue().ghastTexture() != null)
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().ghastTexture()));
    }

    public Map<Identifier, Identifier> getBackgroundTextures() {
        return moodStates.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().backgroundBarTexture()));
    }

    public Map<Identifier, Identifier> getProgressTextures() {
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

            GhastMoodState.MoodRegression regression = entry.getValue().regression();
            if (regression != null && regression.delta() < 0f) {
                return DataResult.error(() ->
                        String.format("Regression delta for %s (%.3f) must not be negative",
                                entry.getKey(), regression.delta()));
            }

            Integer tantrumTick = entry.getValue().tantrumTick();
            if (tantrumTick != null && tantrumTick < 0) {
                return DataResult.error(() ->
                        String.format("Tantrum tick for %s (%d) must not be negative",
                                entry.getKey(), tantrumTick));
            }
        }

        float moodEventRadius = ghastMoodMap.settings().moodEventRadius();
        if (moodEventRadius < 0f) {
            return DataResult.error(() ->
                    String.format("Mood event radius (%.3f) must not be negative", moodEventRadius));
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
        List<Map.Entry<Identifier, GhastMoodState>> sortedStates = ghastMoodMap.moodStates().entrySet().stream()
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