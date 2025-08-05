package ca.corruptdata.moodyghasts.util;

import ca.corruptdata.moodyghasts.MoodyGhasts;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.resources.ResourceLocation;
import com.mojang.serialization.DataResult;

import java.util.LinkedHashMap;
import java.util.Map;

public record MoodThresholds(
        float excited,
        float happy,
        float neutral,
        float sad,
        float angry,
        float enraged
) {
    public static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(MoodyGhasts.MOD_ID, "mood_thresholds");
    
    public static final MoodThresholds DEFAULT = new MoodThresholds(
            20.0f, 40.0f, 60.0f, 70.0f, 80.0f, 100.0f
    );

    public static final Codec<MoodThresholds> CODEC = RecordCodecBuilder.<MoodThresholds>create(instance -> instance.group(
            Codec.FLOAT.fieldOf("excited").forGetter(MoodThresholds::excited),
            Codec.FLOAT.fieldOf("happy").forGetter(MoodThresholds::happy),
            Codec.FLOAT.fieldOf("neutral").forGetter(MoodThresholds::neutral),
            Codec.FLOAT.fieldOf("sad").forGetter(MoodThresholds::sad),
            Codec.FLOAT.fieldOf("angry").forGetter(MoodThresholds::angry),
            Codec.FLOAT.fieldOf("enraged").forGetter(MoodThresholds::enraged)
    ).apply(instance, MoodThresholds::new))
    .comapFlatMap(
    // First argument: Validation function
    thresholds -> {
        float[] values = {
            thresholds.excited, 
            thresholds.happy, 
            thresholds.neutral, 
            thresholds.sad, 
            thresholds.angry, 
            thresholds.enraged
        };
        
        // Using effectively final variable for the lambda
        final float[] prev = {0f};  // Using array to make it effectively final
        
        for (float value : values) {
            if( value < 0f || value > 100f ) {
                return DataResult.error(() ->
                    "Thresholds must be between 0 and 100. Found value " + value);
            }
            if (value < prev[0]) {
                return DataResult.error(() -> 
                    "Thresholds must be in ascending order. Found value " + value + 
                    " after " + prev[0]);
            }
            prev[0] = value;
        }
        
        return DataResult.success(thresholds);
    },
    // Second argument: Identity function for reverse mapping
    thresholds -> thresholds
);

    public float getMoodValue(String mood) {
        return switch (mood) {
            case "excited" -> excited;
            case "happy" -> happy;
            case "neutral" -> neutral;
            case "sad" -> sad;
            case "angry" -> angry;
            case "enraged" -> enraged;
            default -> throw new IllegalArgumentException("Unknown mood: " + mood);
        };
    }

    public Map<String, Float> getMoodMap() {
        return new LinkedHashMap<>() {{
            put("excited", excited);
            put("happy", happy);
            put("neutral", neutral);
            put("sad", sad);
            put("angry", angry);
            put("enraged", enraged);
        }};
    }

}