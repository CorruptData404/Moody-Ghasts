package ca.corruptdata.moodyghasts.mixins;

import net.minecraft.client.gui.Hud;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Arrays;


@Mixin(Hud.ContextualInfo.class)
public abstract class ContextualInfoMixin {

    @Mutable
    @Shadow
    @Final
    private static Hud.ContextualInfo[] $VALUES;

    @Invoker("<init>")
    public static Hud.ContextualInfo moodyghasts$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    static {
        // Guard: only add if not already present (handles hot reload / classloader edge cases)
        boolean alreadyPresent = Arrays.stream($VALUES)
                .anyMatch(e -> e.name().equals("MOODY_VEHICLE"));
        if (!alreadyPresent) {
            Hud.ContextualInfo[] newValues = new Hud.ContextualInfo[$VALUES.length + 1];
            System.arraycopy($VALUES, 0, newValues, 0, $VALUES.length);
            Hud.ContextualInfo entry = moodyghasts$invokeInit("MOODY_VEHICLE", $VALUES.length);
            newValues[$VALUES.length] = entry;
            $VALUES = newValues;
        }
    }
}