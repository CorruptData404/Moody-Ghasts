package ca.corruptdata.moodyghasts.mixins;

import net.minecraft.client.gui.Gui;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Arrays;


@Mixin(Gui.ContextualInfo.class)
public abstract class ContextualInfoMixin {

    @Mutable
    @Shadow
    @Final
    private static Gui.ContextualInfo[] $VALUES;

    @Invoker("<init>")
    public static Gui.ContextualInfo moodyghasts$invokeInit(String internalName, int internalId) {
        throw new AssertionError();
    }

    static {
        // Guard: only add if not already present (handles hot reload / classloader edge cases)
        boolean alreadyPresent = Arrays.stream($VALUES)
                .anyMatch(e -> e.name().equals("MOODY_VEHICLE"));
        if (!alreadyPresent) {
            Gui.ContextualInfo[] newValues = new Gui.ContextualInfo[$VALUES.length + 1];
            System.arraycopy($VALUES, 0, newValues, 0, $VALUES.length);
            Gui.ContextualInfo entry = moodyghasts$invokeInit("MOODY_VEHICLE", $VALUES.length);
            newValues[$VALUES.length] = entry;
            $VALUES = newValues;
        }
    }
}