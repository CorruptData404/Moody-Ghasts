package ca.corruptdata.moodyghasts;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue SHOOT_LOGGING = BUILDER
            .comment("Whether to log everything related to ghast shooting")
            .translation("config.moodyghasts.shoot_logging")
            .define("logGhastShooting", false);

    public static final ModConfigSpec.BooleanValue MOOD_LOGGING = BUILDER
            .comment("Whether to log every change in the ghast's mood. This is a lot of spam, so only enable if you need it.")
            .translation("config.moodyghasts.mood_logging")
            .define("logGhastMood", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}