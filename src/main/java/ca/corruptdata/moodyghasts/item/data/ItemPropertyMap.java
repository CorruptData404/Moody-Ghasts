package ca.corruptdata.moodyghasts.item.data;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.firing_pattern_factories.FiringPatternFactory;
import ca.corruptdata.moodyghasts.entity.happy_ghast.shooting.projectile_factories.ProjectileFactory;
import ca.corruptdata.moodyghasts.registry.ModRegistries;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.slf4j.Logger;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

public class ItemPropertyMap {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;

    /**
     * Bundles a projectile/consumable's current mood value with its (optional) targetMood, so the
     * two always travel together instead of being passed as separate parameters everywhere.
     * Build one via {@link MoodyProjectile#context(float)} or {@link MoodyConsumable#context(float)}.
     */
    public record MoodContext(float moodValue, Optional<Float> targetMood) {
        /** A context with no targetMood - equivalent to how every item behaved before targetMood existed. */
        public static MoodContext of(float moodValue) {
            return new MoodContext(moodValue, Optional.empty());
        }
    }

    /**
     * Reads a mood-scaled value by key, logging a warning and returning 0.0f if the key isn't present.
     * When {@code context}'s {@code targetMood} is present, individual {@link MoodScalingConfig} keys
     * decide for themselves (via their own {@code targetScaling} flag) whether to use it - see
     * {@link MoodScalingConfig#getScaledValue(MoodContext)}.
     */
    private static float resolveScaledFloat(Identifier type, Map<String, MoodScalingConfig> moodScaling,
                                            String key, MoodContext context) {
        MoodScalingConfig scaling = moodScaling.get(key);
        if (scaling == null) {
            LOGGER.warn("'{}' has no moodScaling entry for key '{}' (mood={}) - returning 0.0. " +
                    "Check moody_projectiles_map.json.", type, key, context.moodValue());
            return 0.0f;
        }
        return scaling.getScaledValue(context);
    }

    /** Same as {@link #resolveScaledFloat(Identifier, Map, String, MoodContext)}, rounded to the nearest int. */
    private static int resolveScaledInt(Identifier type, Map<String, MoodScalingConfig> moodScaling,
                                        String key, MoodContext context) {
        return Math.round(resolveScaledFloat(type, moodScaling, key, context));
    }

    /** Shared by any config record with a {@code type} identifier and a {@code moodScaling} map. */
    public interface MoodScalable {
        Identifier type();
        Map<String, MoodScalingConfig> moodScaling();

        /** Reads a mood-scaled value by key. */
        default float getScaled(String key, MoodContext context) {
            return resolveScaledFloat(type(), moodScaling(), key, context);
        }

        /** Same as {@link #getScaled(String, MoodContext)}, rounded to the nearest int. */
        default int getScaledInt(String key, MoodContext context) {
            return resolveScaledInt(type(), moodScaling(), key, context);
        }
    }

    // ============================================================
    // MoodyConsumable - Food items that affect mood
    // ============================================================

    public record MoodyConsumable(
            int count,
            Optional<Identifier> remainderItem,
            float moodDelta,
            Optional<Float> targetMood,
            int consumeTick,
            int rtpDiameter
    ) {

        public static final Codec<MoodyConsumable> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("count",1).forGetter(MoodyConsumable::count),
                Identifier.CODEC.optionalFieldOf("remainderItem").forGetter(MoodyConsumable::remainderItem),
                Codec.FLOAT.fieldOf("moodDelta").forGetter(MoodyConsumable::moodDelta),
                GhastMoodMap.PERCENT.optionalFieldOf("targetMood").forGetter(MoodyConsumable::targetMood),
                Codec.INT.optionalFieldOf("consumeTick",32).forGetter(MoodyConsumable::consumeTick),
                Codec.INT.optionalFieldOf("rtpDiameter",0).forGetter(MoodyConsumable::rtpDiameter)
        ).apply(inst, MoodyConsumable::new));

        public static final DataMapType<Item, MoodyConsumable> DATA_MAP = DataMapType.builder(
                Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_consumables_map"),
                Registries.ITEM,
                CODEC
        ).build();

        /** Bundles {@code actualMood} with this consumable's own {@code targetMood} into one {@link MoodContext}. */
        public MoodContext context(float actualMood) {
            return new MoodContext(actualMood, targetMood);
        }
    }

    // ============================================================
    // ScalingConfig - Defines min/max ranges for mood-based scaling
    // ============================================================

    public record MoodScalingConfig(
            float min,
            float max,
            boolean stepped,
            boolean inverted,
            boolean targetScaling
    ) {

        public static final Codec<MoodScalingConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        Codec.FLOAT.fieldOf("min").forGetter(MoodScalingConfig::min),
                        Codec.FLOAT.fieldOf("max").forGetter(MoodScalingConfig::max),
                        Codec.BOOL.optionalFieldOf("stepped", false).forGetter(MoodScalingConfig::stepped),
                        Codec.BOOL.optionalFieldOf("inverted", false).forGetter(MoodScalingConfig::inverted),
                        Codec.BOOL.optionalFieldOf("targetScaling", true).forGetter(MoodScalingConfig::targetScaling)
                ).apply(inst, MoodScalingConfig::new)
        );

        /**
         * Scales by {@code context.moodValue()}. When {@code context.targetMood()} is present
         * <em>and</em> this key's {@code targetScaling} is {@code true} (the default), the scaling
         * instead peaks (at {@code max}) when the mood value is closest to the target, and falls off
         * toward {@code min} as it moves away in either direction - see
         * {@link #closenessToTarget(float, float)}. Set {@code "targetScaling": false} on a specific
         * key in the JSON to keep it scaling off the raw mood value even when the parent item has a
         * {@code targetMood} configured. With no {@code targetMood} present at all (e.g. a context
         * built via {@link MoodContext#of(float)}), this scales off the raw mood value regardless
         * of the flag.
         */
        public float getScaledValue(MoodContext context) {
            if (min == max)
                return min;

            boolean useTarget = targetScaling && context.targetMood().isPresent();
            float effective = useTarget
                    ? closenessToTarget(context.moodValue(), context.targetMood().get())
                    : context.moodValue();

            if (inverted)
                effective = 1.0f - effective;

            if (stepped) {
                return getSteppedValue(effective);
            } else {
                return getLinearValue(effective);
            }
        }

        /**
         * Maps an actual mood value ({@code [0,1]}) to a {@code [0,1]} "closeness to target" score:
         * {@code 1.0} exactly at {@code targetMood}, tapering linearly to {@code 0.0} at whichever
         * edge of the mood range ({@link GhastMoodMap#MIN}/{@link GhastMoodMap#MAX}) is furthest away.
         */
        private static float closenessToTarget(float moodValue, float targetMood) {
            float distance = Math.abs(moodValue - targetMood);
            float maxDistance = Math.max(targetMood - GhastMoodMap.MIN, GhastMoodMap.MAX - targetMood);
            if (maxDistance <= 0.0f) {
                return 1.0f; // targetMood sits exactly on a boundary; any distance is "maximal"
            }
            return 1.0f - Math.min(1.0f, distance / maxDistance);
        }

        private float getLinearValue(float moodValue) {
            return min + (max - min) * moodValue;
        }

        private float getSteppedValue(float moodValue) {
            float effectiveThreshold = GhastMoodMap.get().getThresholdValueOfMood(moodValue);
            return min + (max - min) * effectiveThreshold;
        }
    }


    // ============================================================
    // ProjectileConfig - Configuration for individual projectiles
    // ============================================================

    public record ProjectileConfig(
            Identifier type,
            Map<String, MoodScalingConfig> moodScaling  // "radius", "strength", etc.
    ) implements MoodScalable {
        public static final Codec<ProjectileConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("type").forGetter(ProjectileConfig::type),
                Codec.unboundedMap(Codec.STRING, MoodScalingConfig.CODEC)
                        .optionalFieldOf("moodScaling", Map.of())
                        .forGetter(ProjectileConfig::moodScaling)
        ).apply(inst, ProjectileConfig::new));

        public float getRadius(MoodContext context) {
            return getScaled("radius", context);
        }

        public float getStrength(MoodContext context) {
            return getScaled("strength", context);
        }
    }

    // ============================================================
    // PatternConfig - Configuration for firing patterns
    // ============================================================

    public record PatternConfig(
            Identifier type,
            Integer chargeDuration,
            Map<String, MoodScalingConfig> moodScaling  // "count", "velocity", "shotDelay", etc.
    ) implements MoodScalable {
        public static final Codec<PatternConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("type").forGetter(PatternConfig::type),
                Codec.INT.optionalFieldOf("chargeDuration", 20).forGetter(PatternConfig::chargeDuration),
                Codec.unboundedMap(Codec.STRING, MoodScalingConfig.CODEC)
                        .optionalFieldOf("moodScaling", Map.of())
                        .forGetter(PatternConfig::moodScaling)
        ).apply(inst, PatternConfig::new));

        // Named convenience wrappers for the common properties.
        public float getVelocity(MoodContext context) {
            return getScaled("velocity", context);
        }

        public float getInaccuracy(MoodContext context) {
            return getScaled("inaccuracy", context);
        }

        public int getCount(MoodContext context) {
            return getScaledInt("count", context);
        }

        public int getShotDelay(MoodContext context) {
            return getScaledInt("shotDelay", context);
        }
    }

    // ============================================================
    // MoodyProjectile - Main projectile configuration record
    // ============================================================

    public record MoodyProjectile(
            int count,
            Optional<Identifier> remainderItem,
            int cooldown,
            float moodDelta,
            Optional<Float> targetMood,
            ProjectileConfig projectile,
            PatternConfig shot
    ) {
        public static final Codec<MoodyProjectile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("count",1).forGetter(MoodyProjectile::count),
                Identifier.CODEC.optionalFieldOf("remainderItem").forGetter(MoodyProjectile::remainderItem),
                Codec.INT.fieldOf("cooldown").forGetter(MoodyProjectile::cooldown),
                Codec.FLOAT.optionalFieldOf("moodDelta", 0.0f).forGetter(MoodyProjectile::moodDelta),
                GhastMoodMap.PERCENT.optionalFieldOf("targetMood").forGetter(MoodyProjectile::targetMood),
                ProjectileConfig.CODEC.fieldOf("projectile").forGetter(MoodyProjectile::projectile),
                PatternConfig.CODEC.fieldOf("shot").forGetter(MoodyProjectile::shot)
        ).apply(inst, MoodyProjectile::new));

        public static final DataMapType<Item, MoodyProjectile> DATA_MAP = DataMapType.builder(
                Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_projectiles_map"),
                Registries.ITEM,
                CODEC
        ).build();

        /** Bundles {@code actualMood} with this item's own {@code targetMood} into one {@link MoodContext}. */
        public MoodContext context(float actualMood) {
            return new MoodContext(actualMood, targetMood);
        }
    }

    // ============================================================
    // Datapack validation - entry point
    // ============================================================

    /** Runs all datapack validation checks below. See MoodyGhasts for where this is called. */
    public static void validateAll() {
        validateMoodScalingKeys();
        validateItemCounts();
        validateExclusiveItemRoles();
    }

    // ============================================================
    // Datapack validation for count
    // ============================================================

    public static void validateItemCounts() {
        for (Map.Entry<ResourceKey<Item>, MoodyProjectile> entry : BuiltInRegistries.ITEM.getDataMap(MoodyProjectile.DATA_MAP).entrySet()) {
            validateCount(entry.getKey(), "moody_projectiles_map", entry.getValue().count());
        }
        for (Map.Entry<ResourceKey<Item>, MoodyConsumable> entry : BuiltInRegistries.ITEM.getDataMap(MoodyConsumable.DATA_MAP).entrySet()) {
            validateCount(entry.getKey(), "moody_consumables_map", entry.getValue().count());
        }
    }

    private static void validateCount(ResourceKey<Item> key, String mapName, int count) {
        Identifier itemId = key.identifier();

        if (count < 0) {
            LOGGER.error("Item '{}' has {} count {} - count must be at least 0.",
                    itemId, mapName, count);
            return;
        }

        Item item = BuiltInRegistries.ITEM.get(itemId).map(Holder.Reference::value).orElseThrow();

        int maxStackSize = item.getDefaultMaxStackSize();
        if (count > maxStackSize) {
            LOGGER.error("Item '{}' has {} count {} but only stacks to {} - " +
                            "this item can never reach the required count and will never be usable.",
                    itemId, mapName, count, maxStackSize);
        }
    }

    // ============================================================
    // Datapack validation for items configured as both projectile and consumable
    // ============================================================

    /** Warns/errors if an item has both a MoodyProjectile and a MoodyConsumable entry. */
    public static void validateExclusiveItemRoles() {
        Map<ResourceKey<Item>, MoodyProjectile> projectiles = BuiltInRegistries.ITEM.getDataMap(MoodyProjectile.DATA_MAP);
        Map<ResourceKey<Item>, MoodyConsumable> consumables = BuiltInRegistries.ITEM.getDataMap(MoodyConsumable.DATA_MAP);

        for (Map.Entry<ResourceKey<Item>, MoodyProjectile> entry : projectiles.entrySet()) {
            if (consumables.get(entry.getKey()) != null)
                LOGGER.error("Item '{}' is configured as both a MoodyProjectile and a MoodyConsumable " +
                                "GhastInteractionHandler can't distinguish which interaction should apply" +
                                "Remove one of the two moody_*_map entries for this item.",
                        entry.getKey().identifier());
        }
    }

    // ============================================================
    // Datapack validation for moodScaling keys
    // ============================================================

    /**
     * Checks {@code moodScaling} keys in moody_projectiles_map.json against what the
     * registered {@link ProjectileFactory} (for {@code projectile.type}) or
     * {@link FiringPatternFactory} (for {@code shot.type}) reports via
     * {@code getRecognizedMoodScalingKeys()}, and logs a warning for:
     * - a {@code projectile.type}/{@code shot.type} with no matching registry entry,
     * - a moodScaling key present in the JSON that its factory doesn't recognize, and
     * - a key the factory recognizes but that's missing from the JSON entry.
     */
    public static void validateMoodScalingKeys() {
        Registry<ProjectileFactory> projectileFactories =
                ModRegistries.PROJECTILE_FACTORY_REGISTER.getRegistry().get();
        Registry<FiringPatternFactory> shootingBehaviours =
                ModRegistries.FIRING_PATTERN_FACTORY_REGISTER.getRegistry().get();

        for (Map.Entry<ResourceKey<Item>, MoodyProjectile> entry : BuiltInRegistries.ITEM.getDataMap(MoodyProjectile.DATA_MAP).entrySet()) {
            Identifier itemId = entry.getKey().identifier();
            MoodyProjectile config = entry.getValue();

            validateSection(itemId, "projectile", config.projectile().type(), config.projectile().moodScaling(),
                    projectileFactories, "projectile factory registry", ProjectileFactory::getRecognizedMoodScalingKeys);
            validateSection(itemId, "shot", config.shot().type(), config.shot().moodScaling(),
                    shootingBehaviours, "shooting behaviour registry", FiringPatternFactory::getRecognizedMoodScalingKeys);
        }
    }

    private static <F> void validateSection(Identifier itemId, String section, Identifier type,
                                            Map<String, MoodScalingConfig> moodScaling,
                                            Registry<F> factoryRegistry, String registryLabel,
                                            Function<F, Set<String>> recognizedKeysFn) {
        F factory = factoryRegistry.get(type)
                .map(Holder.Reference::value)
                .orElse(null);

        if (factory == null) {
            LOGGER.warn("Item '{}' has {} type '{}' with no matching entry in the {} - " +
                            "check for a typo, or a mod whose factory failed to register.",
                    itemId, section, type, registryLabel);
            return;
        }

        Set<String> recognized = recognizedKeysFn.apply(factory);

        for (String key : moodScaling.keySet()) {
            if (!recognized.contains(key)) {
                LOGGER.warn("Item '{}' {} type '{}' has unrecognized moodScaling key '{}' - " +
                        "likely a typo, this entry will be ignored.", itemId, section, type, key);
            }
        }

        for (String key : recognized) {
            if (!moodScaling.containsKey(key)) {
                LOGGER.warn("Item '{}' {} type '{}' is missing moodScaling key '{}' - " +
                        "code that reads it will fall back to 0.0 at runtime.", itemId, section, type, key);
            }
        }
    }
}