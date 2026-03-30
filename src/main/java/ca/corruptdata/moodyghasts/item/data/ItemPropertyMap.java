
package ca.corruptdata.moodyghasts.item.data;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import ca.corruptdata.moodyghasts.entity.happy_ghast.data.GhastMoodMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import org.slf4j.Logger;

import java.util.Map;

public class ItemPropertyMap {

    private static final Logger LOGGER = MoodyGhasts.LOGGER;

    // ============================================================
    // MoodyConsumable - Food items that affect mood
    // ============================================================

    public record MoodyConsumable(float moodDelta) {
        public static final Codec<MoodyConsumable> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.FLOAT.fieldOf("moodDelta").forGetter(MoodyConsumable::moodDelta)
        ).apply(inst, MoodyConsumable::new));

        public static final DataMapType<Item, MoodyConsumable> DATA_MAP = DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_consumables_map"),
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
            ResourceLocation type,
            Map<String, MoodScalingConfig> moodScaling  // "radius", "strength", etc.
    ) {
        public static final Codec<ProjectileConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter(ProjectileConfig::type),
                Codec.unboundedMap(Codec.STRING, MoodScalingConfig.CODEC)
                        .optionalFieldOf("moodScaling", Map.of())
                        .forGetter(ProjectileConfig::moodScaling)
        ).apply(inst, ProjectileConfig::new));

        // Helper methods for common projectile properties
        public float getRadius(float moodValue) {
            MoodScalingConfig radiusScaling = moodScaling.get("radius");
            return radiusScaling != null ? radiusScaling.getScaledValue(moodValue) : 3.0f;
        }

        public float getStrength(float moodValue) {
            MoodScalingConfig strengthScaling = moodScaling.get("strength");
            return strengthScaling != null ? strengthScaling.getScaledValue(moodValue) : 1.0f;
        }
    }

    // ============================================================
    // ShotConfig - Configuration for shooting behavior
    // ============================================================

    public record ShotConfig(
            ResourceLocation type,
            Integer chargeDuration,
            Map<String, MoodScalingConfig> moodScaling  // "count", "delay", etc.
    ) {
        public static final Codec<ShotConfig> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                ResourceLocation.CODEC.fieldOf("type").forGetter(ShotConfig::type),
                Codec.INT.optionalFieldOf("chargeDuration", 20).forGetter(ShotConfig::chargeDuration),
                Codec.unboundedMap(Codec.STRING, MoodScalingConfig.CODEC)
                        .optionalFieldOf("moodScaling", Map.of())
                        .forGetter(ShotConfig::moodScaling)
        ).apply(inst, ShotConfig::new));

        // Helper methods for common shot properties
        public float getVelocity(float moodValue) {
            MoodScalingConfig velocityScaling = moodScaling.get("velocity");
            return velocityScaling != null ? velocityScaling.getScaledValue(moodValue) : 1.5f;
        }

        public float getInaccuracy(float moodValue) {
            MoodScalingConfig inaccuracyScaling = moodScaling.get("inaccuracy");
            return inaccuracyScaling != null ? inaccuracyScaling.getScaledValue(moodValue) : 0.0f;
        }

        public int getCount(float moodValue) {
            MoodScalingConfig countScaling = moodScaling.get("count");
            return countScaling != null ? Math.round(countScaling.getScaledValue(moodValue)) : 1;
        }
    }

    // ============================================================
    // MoodyProjectile - Main projectile configuration record
    // ============================================================

    public record MoodyProjectile(
            int cooldown,
            float moodDelta,
            ProjectileConfig projectile,
            ShotConfig shot
    ) {
        public static final Codec<MoodyProjectile> CODEC = RecordCodecBuilder.create(inst -> inst.group(
                Codec.INT.fieldOf("cooldown").forGetter(MoodyProjectile::cooldown),
                Codec.FLOAT.optionalFieldOf("moodDelta", 0.0f).forGetter(MoodyProjectile::moodDelta),
                ProjectileConfig.CODEC.fieldOf("projectile").forGetter(MoodyProjectile::projectile),
                ShotConfig.CODEC.fieldOf("shot").forGetter(MoodyProjectile::shot)
        ).apply(inst, MoodyProjectile::new));

        public static final DataMapType<Item, MoodyProjectile> DATA_MAP = DataMapType.builder(
                ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "moody_projectiles_map"),
                Registries.ITEM,
                CODEC
        ).build();
    }
}