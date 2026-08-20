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

    /** Reads a mood-scaled value by key, logging a warning and returning 0.0f if the key isn't present. */
    private static float resolveScaledFloat(Identifier type, Map<String, MoodScalingConfig> moodScaling,
                                            String key, float moodValue) {
        MoodScalingConfig scaling = moodScaling.get(key);
        if (scaling == null) {
            LOGGER.warn("'{}' has no moodScaling entry for key '{}' (mood={}) - returning 0.0. " +
                    "Check moody_projectiles_map.json.", type, key, moodValue);
            return 0.0f;
        }
        return scaling.getScaledValue(moodValue);
    }

    /** Same as {@link #resolveScaledFloat}, rounded to the nearest int. */
    private static int resolveScaledInt(Identifier type, Map<String, MoodScalingConfig> moodScaling,
                                        String key, float moodValue) {
        return Math.round(resolveScaledFloat(type, moodScaling, key, moodValue));
    }

    /** Shared by any config record with a {@code type} identifier and a {@code moodScaling} map. */
    public interface MoodScalable {
        Identifier type();
        Map<String, MoodScalingConfig> moodScaling();

        /** Reads a mood-scaled value by key. */
        default float getScaled(String key, float moodValue) {
            return resolveScaledFloat(type(), moodScaling(), key, moodValue);
        }

        /** Same as {@link #getScaled}, rounded to the nearest int. */
        default int getScaledInt(String key, float moodValue) {
            return resolveScaledInt(type(), moodScaling(), key, moodValue);
        }
    }

    // ============================================================
    // MoodyConsumable - Food items that affect mood
    // ============================================================

    public record MoodyConsumable(
            int count,
            Optional<Identifier> remainderItem,
            float moodDelta,
            int consumeTick,
            int rtpDiameter
    ) {

        public static final Codec<MoodyConsumable> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("count",1).forGetter(MoodyConsumable::count),
                Identifier.CODEC.optionalFieldOf("remainderItem").forGetter(MoodyConsumable::remainderItem),
                Codec.FLOAT.fieldOf("moodDelta").forGetter(MoodyConsumable::moodDelta),
                Codec.INT.optionalFieldOf("consumeTick",32).forGetter(MoodyConsumable::consumeTick),
                Codec.INT.optionalFieldOf("rtpDiameter",0).forGetter(MoodyConsumable::rtpDiameter)
        ).apply(inst, MoodyConsumable::new));

        public static final DataMapType<Item, MoodyConsumable> DATA_MAP = DataMapType.builder(
                Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_consumables_map"),
                Registries.ITEM,
                CODEC
        ).build();
    }

    // ============================================================
    // ScalingConfig - Defines min/max ranges for mood-based scaling
    // ============================================================

    public record MoodScalingConfig(
            float min,
            float max,
            boolean stepped,
            boolean inverted
    ) {

        public static final Codec<MoodScalingConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                        Codec.FLOAT.fieldOf("min").forGetter(MoodScalingConfig::min),
                        Codec.FLOAT.fieldOf("max").forGetter(MoodScalingConfig::max),
                        Codec.BOOL.optionalFieldOf("stepped", false).forGetter(MoodScalingConfig::stepped),
                        Codec.BOOL.optionalFieldOf("inverted", false).forGetter(MoodScalingConfig::inverted)
                ).apply(inst, MoodScalingConfig::new)
        );

        public float getScaledValue(float moodValue) {
            if (min == max)
                return min;
            if (inverted)
                moodValue = 1.0f - moodValue;
            if (stepped) {
                return getSteppedValue(moodValue);
            } else {
                return getLinearValue(moodValue);
            }
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

        public float getRadius(float moodValue) {
            return getScaled("radius", moodValue);
        }

        public float getStrength(float moodValue) {
            return getScaled("strength", moodValue);
        }
    }

    // ============================================================
    // PatternConfig - Configuration for firing patterns
    // ============================================================

    public record PatternConfig(
            Identifier type,
            Integer chargeDuration,
            Map<String, MoodScalingConfig> moodScaling  // "count", "velocity", etc.
    ) implements MoodScalable {
        public static final Codec<PatternConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Identifier.CODEC.fieldOf("type").forGetter(PatternConfig::type),
                Codec.INT.optionalFieldOf("chargeDuration", 20).forGetter(PatternConfig::chargeDuration),
                Codec.unboundedMap(Codec.STRING, MoodScalingConfig.CODEC)
                        .optionalFieldOf("moodScaling", Map.of())
                        .forGetter(PatternConfig::moodScaling)
        ).apply(inst, PatternConfig::new));

        // Named convenience wrappers for the common properties.
        public float getVelocity(float moodValue) {
            return getScaled("velocity", moodValue);
        }

        public float getInaccuracy(float moodValue) {
            return getScaled("inaccuracy", moodValue);
        }

        public int getCount(float moodValue) {
            return getScaledInt("count", moodValue);
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
            ProjectileConfig projectile,
            PatternConfig shot
    ) {
        public static final Codec<MoodyProjectile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.optionalFieldOf("count",1).forGetter(MoodyProjectile::count),
                Identifier.CODEC.optionalFieldOf("remainderItem").forGetter(MoodyProjectile::remainderItem),
                Codec.INT.fieldOf("cooldown").forGetter(MoodyProjectile::cooldown),
                Codec.FLOAT.optionalFieldOf("moodDelta", 0.0f).forGetter(MoodyProjectile::moodDelta),
                ProjectileConfig.CODEC.fieldOf("projectile").forGetter(MoodyProjectile::projectile),
                PatternConfig.CODEC.fieldOf("shot").forGetter(MoodyProjectile::shot)
        ).apply(inst, MoodyProjectile::new));

        public static final DataMapType<Item, MoodyProjectile> DATA_MAP = DataMapType.builder(
                Identifier.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_projectiles_map"),
                Registries.ITEM,
                CODEC
        ).build();
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