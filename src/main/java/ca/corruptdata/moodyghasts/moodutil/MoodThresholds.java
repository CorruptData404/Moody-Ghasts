package ca.corruptdata.moodyghasts.moodutil;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.neoforged.neoforge.registries.datamaps.DataMapType;

public record MoodThresholds(
        float excited,
        float happy,
        float neutral,
        float sad,
        float angry
) {
    // ============================================================
    // Constants
    // ============================================================
    
    public static final float MAX = 100f;
    public static final float MIN = 0f;
    private static final Codec<Float> PERCENT = Codec.floatRange(MIN, MAX);

    // ============================================================
    // Codec Definition
    // ============================================================

    public static final Codec<MoodThresholds> CODEC = RecordCodecBuilder.<MoodThresholds>create(inst -> inst.group(
            PERCENT.fieldOf("excited").forGetter(MoodThresholds::excited),
            PERCENT.fieldOf("happy").forGetter(MoodThresholds::happy),
            PERCENT.fieldOf("neutral").forGetter(MoodThresholds::neutral),
            PERCENT.fieldOf("sad").forGetter(MoodThresholds::sad),
            PERCENT.fieldOf("angry").forGetter(MoodThresholds::angry)
    ).apply(inst, MoodThresholds::new)).validate(MoodThresholds::validate);

    public static final DataMapType<EntityType<?>, MoodThresholds> MOOD_THRESHOLDS = DataMapType.builder(
            ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "mood_thresholds"),
            Registries.ENTITY_TYPE,
            CODEC
    ).synced(CODEC, false).build();

    // ============================================================
    // Public Methods
    // ============================================================

    public Mood getMoodFromValue(float value) {
        if (value <= excited()) return Mood.EXCITED;
        if (value <= happy())   return Mood.HAPPY;
        if (value <= neutral()) return Mood.NEUTRAL;
        if (value <= sad())     return Mood.SAD;
        if (value <= angry())   return Mood.ANGRY;
        return Mood.ENRAGED;
    }

    // ============================================================
    // Private Methods
    // ============================================================

    private DataResult<MoodThresholds> validate() {
        float prev = 0f;
        float[] values = { excited, happy, neutral, sad, angry };
        Mood[] moods = Mood.values();

        for (int i = 0; i < values.length; i++) {
            float current = values[i];
            if (current < prev) {
                float finalPrev = prev;
                int finalI = i;
                return DataResult.error(() ->
                        "Mood thresholds must be ascending. " +
                                moods[finalI] + " (" + current + ") < previous (" + finalPrev + ")"
                );
            }
            prev = current;
        }
        return DataResult.success(this);
    }
}