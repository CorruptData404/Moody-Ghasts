package ca.corruptdata.moodyghasts.moodutil;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import net.minecraft.resources.ResourceLocation;

import java.util.EnumMap;
import java.util.Map;

public record MoodThresholds(EnumMap<Mood, Float> thresholds) {

    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "data/moodyghasts/mood_thresholds.json");

    public static final MoodThresholds DEFAULT;
    static {
        EnumMap<Mood, Float> map = new EnumMap<>(Mood.class);
        map.put(Mood.EXCITED, 20f);
        map.put(Mood.HAPPY, 40f);
        map.put(Mood.NEUTRAL, 60f);
        map.put(Mood.SAD, 70f);
        map.put(Mood.ANGRY, 80f);
        map.put(Mood.ENRAGED, 100f);
        DEFAULT = new MoodThresholds(map);
    }

    /** Simple getter */
    public float get(Mood mood) {
        return thresholds.get(mood);
    }

    /** Returns a copy of the map */
    public Map<Mood, Float> getMap() {
        return new EnumMap<>(thresholds);
    }

    /** Validate thresholds: all 0–100 and ascending */
    private DataResult<MoodThresholds> validate() {
        float prev = 0f;
        for (Mood mood : Mood.values()) {
            float current = thresholds.get(mood); // auto-unboxed, cannot be null
            float last = prev;

            if (current < 0f || current > 100f) {
                return DataResult.error(() -> "Threshold for " + mood + " must be 0–100, found " + current);
            }
            if (current < prev) {
                return DataResult.error(() -> "Thresholds must be ascending. Found " + current + " after " + last);
            }

            prev = current;
        }
        return DataResult.success(this);
    }

    /** Codec: serializes EnumMap<Mood, Float> with automatic validation */
    public static final Codec<MoodThresholds> CODEC =
            Codec.unboundedMap(Mood.CODEC, Codec.FLOAT)
                    .xmap(
                            map -> new MoodThresholds(new EnumMap<>(map)),
                            MoodThresholds::getMap
                    )
                    .flatXmap(MoodThresholds::validate, DataResult::success);
}
