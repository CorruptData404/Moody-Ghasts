package ca.corruptdata.moodyghasts.moodutil;

import com.mojang.serialization.Codec;

import java.util.Locale;

public enum Mood {
    EXCITED,
    HAPPY,
    NEUTRAL,
    SAD,
    ANGRY,
    ENRAGED;

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }
}